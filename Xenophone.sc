//generative system to create imagined alien music, or even alien music imagined by aliens observing Earth from afar and imagining Earth music

Xenophone {
	classvar <>generationcounter;
	classvar <>basepath;

	var <>basegroup, <>synthgroup, <>fxgroup;
	var <>finalsynth;
	var <>s;
	var recordbuffer, recordsynth;
	var effectsynth;

	var starttime;

	var <routine, <universeroutine;

	//stereo convolution reverb
	var convolutionreverb;
	var irspectrumL, irspectrumR;



	//musical parameters
	var memorysize;

	//grid
	var iois;
	var numcyclemarkers;
	var cycle;
	var importance;
	var cycleduration;

	var numvoices;

	//var allowtempochange

	//pitch
	var frequencies;
	var freqsubsets, freqsubsetbysection;
	var allowtransposition;
	var allowdrone, dronechance;
	var serialflag;
	var envelopes; //common to all voices, or per voice


	//NRT
	var score, serveroptions;


	*initClass {

		Xenophone.generationcounter = 0;

		Xenophone.basepath = Platform.defaultTempDir; //"~/Desktop/xeno/";

	}

	*new {|foo=0|

		^super.new.initXenophone(foo);
	}


	initXenophone {|foo = 0|

		s = Server.default;

		//boot server if not running already

		s.waitForBoot({

			Xenophone.initSynthDefs(2,0); //2 channels, 0 input offset

			s.sync;

			basegroup = Group(); //.basicNew(s,1);

			synthgroup = Group.head(basegroup);

			fxgroup = Group.after(synthgroup);
			//
			//			//for mixdown and limiter etc
			finalsynth = Synth.tail(basegroup,\XenophoneMasterFX);

			s.sync;

		});

		starttime = Main.elapsedTime;

	}



	record {|numChannels=2 which=0|

		{

			recordbuffer = Buffer.alloc(s, 65536,numChannels);

			s.sync;

			recordbuffer.write("~/Music/SuperCollider Recordings/Xenophone"++(which.asString)++".wav".standardizePath, "wav", "int24", 0, 0, true);

			s.sync;
			// create the diskout node; making sure it comes after the source
			recordsynth = Synth.tail(RootNode(s), \Xenophonediskout, [\bufnum, recordbuffer]);

		}.fork;

	}

	stopRecord {

		if(recordsynth.notNil) {

			recordsynth.free;

			{

				recordbuffer.close;

				s.sync;

				recordbuffer.free;

				recordsynth= nil;

			}.fork;

		};



	}




	//Creates grid and available time units
	//LATER: allow tempo change? What if an alien species could track more than one metre at once?
	prepareRhythmicMaterials {

		memorysize = rrand(2,50);
		//number of different iois reflects different memories for time units
		iois = Array.exprand(memorysize,0.03,1.8);

		numcyclemarkers = rrand(3,40);
		cycle = Array.fill(numcyclemarkers,{iois.choose});

		importance = cycle.collect{|val,i|  [rrand(0,4),rrand(-2,2),rrand(0,10),rrand(-5,0)].wchoose([0.6,0.2,0.1,0.1]) };

		numvoices = rrand(2,15);

		cycleduration = cycle.sum;

	}



	preparePitchMaterials {

		var temp;

		frequencies = Array.exprand(memorysize*rrand(2,4),40,1000);

		freqsubsets = {frequencies.scramble.copyRange(0,rrand(memorysize.div(2),memorysize).max(1) -1)}!memorysize;

		allowtransposition = 0.4.coin;

		 //0.2.coin;
		dronechance = [0.2,rrand(0.01,1.0),exprand(0.01,1.0)].choose;
		allowdrone = {dronechance.coin}!numvoices;
		serialflag = 0.3.coin;

		if(0.4.coin) {allowdrone = {false}!numvoices;};
		if(0.1.coin) {allowdrone = {true}!numvoices;};

		freqsubsetbysection = (if(0.5.coin) {Pseq(freqsubsets,inf)}{Pxrand(freqsubsets,inf)}).asStream;

		//force a form
		if(0.4.coin) {

			temp = rrand(freqsubsets.size.div(3),freqsubsets.size).min(2) -1;
			//allow repetitions of the same subset in a complicated form
			temp = Pxrand((0..temp),memorysize).asStream.nextN(memorysize);

			freqsubsetbysection = Pseq(freqsubsets.at(temp),inf).asStream;

		}

		//frequenciesnow = (if(serialflag) {Pseq(frequenciesnow,inf)}{Pxrand(frequenciesnow,inf)}).asStream;
		//variationnow.postcs;

	}



	universe {
		var planet = 1;

		if(universeroutine.notNil) {universeroutine.stop;};

		universeroutine = {
			inf.do{
			var durationnow = rrand(30.0,300.0);

				["exoplanet",planet,"duration",durationnow].postln;

			this.play(false, durationnow);

			(durationnow + 5.0).wait;
			};
		}.fork;

	}



	play {|nrtflag = false duration = 200 effectdef nrtaction document=true|


		var tonalorpercussive;
		var numvariations = rrand(1,10);
		var numalready;
		var patterns;
		var panplan;
		var panpositions;

		var nrttime = 0.0;
		var filename;
		var durationmodel = 2.rand; //0 is use fixed durations
		var fixedduration = [0.5, rrand(0.1,1.0), exprand(0.1,2.0), {rrand(0.1,1.0)},{exprand(0.1,2.0)}].choose;
		var envelopetemp;
		var alwayspercussive = 0.5.coin; //no long attacks for percussive voicess

		var toneplus = [0,15].choose;
		var generationcounternow;

		score = List[];

		generationcounternow = Xenophone.generationcounter;

		Xenophone.generationcounter = Xenophone.generationcounter + 1;




		if(effectsynth.notNil) {effectsynth.release;  effectsynth = nil;};

		effectdef = effectdef ?? {if(0.5.coin){"XenophoneFX" ++ (rrand(1,7));}{nil}};

		if(effectdef.notNil) {

			if(nrtflag) {

				score.add([0.0, [ \s_new, effectdef, -1, 1, 0]]);

			} {

				effectsynth = Synth.tail(fxgroup,effectdef)

			};

		};

		//always kill any existing convolution reverb
		if(convolutionreverb.notNil) {

			convolutionreverb.free;
			convolutionreverb = nil;

			irspectrumL.free;
			irspectrumR.free;

		};




		if(routine.notNil) {routine.stop;};

		this.prepareRhythmicMaterials;
		this.preparePitchMaterials;

		tonalorpercussive = {0.5.coin}!numvoices; //2.rand

		if(0.1.coin) {tonalorpercussive = {true}!numvoices;};
		if(0.1.coin) {tonalorpercussive = {false}!numvoices;};

		numalready = {0}!(importance.size);

		//panplan = 3.rand;
		//0 = fixed  1=moving?

		panpositions = {1.0.rand2}!numvoices;

		if(0.2.coin) //even spread
		{panpositions = {|i|  (i/(numvoices-1))*2.0-1.0 }!numvoices;};

		if(0.2.coin) //a few groups
		{
			var numgroups = exprand(2,numvoices).round(1.0).asInteger;

			panpositions = {|i|  ((i%numgroups)/(numgroups-1))*2.0-1.0 }!numvoices;

		};

		if(0.1.coin) //mono, all central
		{panpositions = {0.0}!numvoices;};



		//per voicessL
		envelopetemp = {[
				[0.005,0.01,rrand(0.01,1.0),exprand(0.01,2.0)].choose, //attack
				[0.5,rrand(0.1,1.0),exprand(0.1,2.0)].choose, //sustain
				[rrand(0.003,0.1),rrand(0.1,0.8),0.5,rrand(0.01,0.5),exprand(0.01,2.0)].choose, //release
		]};

		[\testenv, envelopetemp.value, envelopetemp.value, envelopetemp.value].postcs;

		//attacktime = 0.01 sustaintime = 0.5 releasetime = 0.5
		//first option: same all voices

		//else different each voice
		if(0.7.coin) {

			envelopetemp = envelopetemp.value; //fix value

			//{[1,2,3]}.value

		};

		envelopes = {envelopetemp.value}!numvoices;

		[\envelopes,envelopes].postcs;






		patterns = Array.fill(numvoices*numvariations,{var list = List[];

			var inverseimport = 0.1.coin;
			var dontdeemphalready = 0.5.coin;

			importance.do{|import,i|

				var already = numalready[i];
				var deemphasisealready = (1+already).reciprocal;

				if(dontdeemphalready) {deemphasisealready = 1.0;};

				if(inverseimport) {import = import.neg;};

				if(import>=0) {

					if((import * deemphasisealready * 0.3).coin) {

						list.add(i);
						numalready[i] = numalready[i] + 1;

					}

				}

			};

			if(list.isEmpty) {

				list = Array.fill(rrand(3,numcyclemarkers),{numcyclemarkers.rand;}).asSet.asArray.sort;

				//[\empty, list].postcs;
			};

			list
		});


		//possible speed up or slow down for humming bird or tortoise cognition style
		if (0.5.coin) {
			var speedmult =  ([rrand(0.1,1.0),rrand(1.0,5.0)].choose);
			[\speedchange,speedmult].postcs;

			cycle = cycle * speedmult;

			cycleduration = cycle.sum;

		};


		//patterns.postcs;

		[\numvoices,numvoices,\numvariations,numvariations, \cyclelength, cycleduration].postcs;

		if(document) {
		this.document(Xenophone.basepath++"xenodocument"++generationcounternow++".jpg",nrtflag);
		};


		if(nrtflag) {

			filename = Xenophone.basepath++"xenomusic_"++generationcounternow++".wav";

			filename.postln;

			//score = List[];
		};


		//playback

		//should vary patterns sometimes? Sub more in and out?

		routine = {

			Xenophone.initSynthDefs(2,0); //2 channels, 0 input offset

			s.sync;

			if(0.5.coin) {

				var irpath = PathName(PathName(Xenophone.filenameSymbol.asString).pathOnly ++ "IR").entries.collect{|val| val.fullPath}.choose;

				if(nrtflag) {
					//score.postcs;

					//score = score ++ (this.setConvolutionReverbNRT(irpath));

					this.setConvolutionReverbNRT(irpath);

					//score.postcs;

					"conv done".postln;

				} {
					this.setConvolutionReverb(irpath);
				};

			};



			starttime = Main.elapsedTime;

			if(nrtflag,{var numcycles = (duration/cycleduration).roundUp.asInteger; duration = (numcycles * cycleduration) + 10.0; numcycles},inf).do{|whichcycle|

				var variationnow = {|j| ([0,rrand(0,1.min(numvariations-1)),rrand(0,numvariations.div(2)),rrand(0,numvariations-1)].choose * numvoices) + j}!numvoices;
				var patternsnow = patterns.at(variationnow);

				//could make serial/sequential, or wxrand here rather than free choice
				var frequenciesnow = freqsubsetbysection.next; //freqsubsets.choose;

				var dronefreq = {frequenciesnow.choose}!numvoices;//if needed, one per voice
				var dronessustain = {0.5.coin}!numvoices; //{0.5.coin}!numvoices;

				var cyclepos = 0.0;


				//[\nrttime, nrttime, \cyclenum, whichcycle, \numcycles, (duration/cycleduration).roundUp.asInteger, \cycleduration, cycleduration, \duration, duration].postln;

				//can alter sectionally at this point, for instance, different tonal/percussive sets


				frequenciesnow = (if(serialflag) {Pseq(frequenciesnow,inf)}{Pxrand(frequenciesnow,inf)}).asStream;
				//variationnow.postcs;

				cycle.do{|ioi,i|
					var cycledurleft = cycleduration - cyclepos;

					patternsnow.do{|patt,j|

						var freqnow;
						//fixedduration.value
						var durnow = if(durationmodel==0, {envelopes[j][1]}, ioi); //hard to calculate if depends on checking all slots


						//patt = patterns[0];

						//var patt = patterns[variationnow[j]];

						if(patt.includes(i)) {

							freqnow = frequenciesnow.next;


							if(allowdrone[j]) {freqnow = dronefreq[j]};

							//need sustain mechanism, set durations right now


							if(nrtflag) {

								//was tonal part 0.01 amp, percussive 0.2

								if(tonalorpercussive[j]) {

									score.add([nrttime, [ \s_new, \Xenophonetone ++ (toneplus+j), -1, 0, 0, \freq, freqnow, \amp, 0.02, \pan, panpositions[j], \attacktime, envelopes[j][0],\sustaintime, durnow, \releasetime, envelopes[j][2]]]);

								}
								{


									//if(alwayspercussive)
									// \attacktime, if(alwayspercussive,0.001,envelopes[j][0]),\sustaintime, durnow.min(0.1)s, \releasetime, envelopes[j][2]

									score.add([nrttime, [ \s_new, \Xenophonepercussion ++ j, -1, 0, 0, \amp, 0.2, \pan, panpositions[j],\attacktime, if(alwayspercussive,0.001,envelopes[j][0]),\sustaintime, durnow.min(0.1), \releasetime, envelopes[j][2]]]);


								};



							} {

								s.bind {

									if(tonalorpercussive[j]) {

										//[\percussive, tonalorpercussive[j]].postln;

										Synth.head(synthgroup,\Xenophonetone ++ (toneplus+j),[\freq,freqnow, \amp, 0.02, \pan, panpositions[j], \attacktime, envelopes[j][0],\sustaintime, durnow, \releasetime, envelopes[j][2]]);

									}
									{
										//[\tonal, tonalorpercussive[j]].postln;
										Synth.head(synthgroup,\Xenophonepercussion ++ j,[\amp, 0.2, \pan, panpositions[j],\attacktime, if(alwayspercussive,0.001,envelopes[j][0]),\sustaintime, durnow.min(0.1), \releasetime, envelopes[j][2]]);


									};
									//Synth(\alienpercussion0);


								};

							};

						};

						//[3,5,7].includes(3)

					};


					nrttime = nrttime + ioi;

					if(nrtflag) {

					} {
						ioi.wait;
					};

					cyclepos = cyclepos + ioi;

				};



			};


			if(nrtflag) {

				"NRT render".postln;

				score.add([duration, [\c_set, 0, 0]]);

				score.postcs;

				serveroptions = ServerOptions.new;
				serveroptions.numOutputBusChannels = 2; // stereo output
				serveroptions.verbosity = -2;

				//Score.recordNRT(score, "help-oscFile", filename, headerFormat:"WAV", sampleFormat:"int16", options: serveroptions); // synthesize

				//recordNRTSCMIR
				Score.recordNRT(score,Platform.defaultTempDir +/+ "help-oscFile", filename, headerFormat:"WAV", sampleFormat:"int16", options: serveroptions,action:{nrtaction.(filename)}); // synthesize

			};

		}.fork;




	}




	stop {

		if(universeroutine.notNil){universeroutine.stop;};

		routine.stop;

		if(convolutionreverb.notNil) {

			convolutionreverb.free;

			irspectrumL.free;
			irspectrumR.free;

		};


		if(effectsynth.notNil) {

			effectsynth.free;

		};

	}


	//pass in ir file path
	setConvolutionReverb {|irpath|

		var irbufferL, irbufferR;
		// also 4096 works on my machine; 1024 too often and amortisation too pushed, 8192 more high load FFT
		var fftsize = 2048, bufsize, bufsize2;
		var numchannels;
		//var s = Server.default;


		[\setConvolutionReverb,irpath].postln;


		numchannels = SoundFile.openRead(irpath).numChannels;


		if(convolutionreverb.notNil) {

			convolutionreverb.free;

			irspectrumL.free;
			irspectrumR.free;

		};

		{

			irbufferL = Buffer.readChannel(s,irpath,channels:[0]); // "/Volumes/data/audio/ir/ir2.wav"

	if(numchannels<2) {

			irbufferR = Buffer.readChannel(s,irpath,channels:[0]);
		} {

			irbufferR = Buffer.readChannel(s,irpath,channels:[1]);
		};

			 //Buffer.read(s, irpath);

			s.sync;

			bufsize = PartConv.calcBufSize(fftsize, irbufferL);
			bufsize2 = PartConv.calcBufSize(fftsize, irbufferR);

			irspectrumL = Buffer.alloc(s, bufsize, 1);
			irspectrumL.preparePartConv(irbufferL, fftsize);

			irspectrumR = Buffer.alloc(s, bufsize2, 1);
			irspectrumR.preparePartConv(irbufferR, fftsize);

			s.sync;

			irbufferL.free; // don't need time domain data anymore, just needed spectral version
			irbufferR.free;

			convolutionreverb = Synth.tail(fxgroup,\XenophonePartConv,[\in,0,\out,0,\bufnumL,irspectrumL,\bufnumR,irspectrumR,\amp,0.3]);

		}.fork;

	}


	//inside a fork already
	//pass in ir file path
	setConvolutionReverbNRT {|irpath|

		var irbufferL, irbufferR;
		// also 4096 works on my machine; 1024 too often and amortisation too pushed, 8192 more high load FFT
		var fftsize = 2048, bufsize, bufsize2;
		//var s = Server.default;
		//var score = List[];
		var soundfile, numframes, numchannels;

		[\setConvolutionReverbNRT,irpath].postln;

		if(convolutionreverb.notNil) {

			convolutionreverb.free;

			irspectrumL.free;
			irspectrumR.free;

		};

		//Server Command Reference

		soundfile = SoundFile.openRead(irpath);
		//could check num channels here

		numframes = soundfile.numFrames;
		numchannels = soundfile.numChannels;

		//PartConv.calcBufSize(fftsize, irbufferL);
		bufsize = fftsize * ((numframes/(fftsize.div(2))).roundUp);
		bufsize2 = bufsize; //fftsize * ((numframes/(fftsize.div(2))).roundUp);

		score.add([0.0,\b_allocReadChannel, 0, irpath, 0, 0, 0]);


		if(numchannels<2) {
			score.add([0.0,\b_allocReadChannel, 1, irpath, 0, 0, 0]);
		} {
			score.add([0.0,\b_allocReadChannel, 1, irpath, 0, 0, 1]);
		};


		//bufsize = PartConv.calcBufSize(fftsize, irbufferL);
		//bufsize2 = PartConv.calcBufSize(fftsize, irbufferR);

		//s.sync;

		score.add([0.0,\b_alloc, 2, bufsize, 1]);
		score.add([0.0,\b_alloc, 3, bufsize2, 1]);

		//irspectrumL = Buffer.alloc(s, bufsize, 1);
		//irspectrumL.preparePartConv(irbufferL, fftsize);
		//irspectrumR = Buffer.alloc(s, bufsize2, 1);
		//irspectrumR.preparePartConv(irbufferR, fftsize);

		score.add([0.0,\b_gen, 2, "PreparePartConv", 0, fftsize]);
		score.add([0.0,\b_gen, 3, "PreparePartConv", 1, fftsize]);

		//irbufferL.free; // don't need time domain data anymore, just needed spectral version
		//irbufferR.free;

		//convolutionreverb = Synth.tail(fxgroup,\XenophonePartConv,[\in,0,\out,0,\bufnumL,irspectrumL,\bufnumR,irspectrumR,\amp,0.3]);
		score.add([0.0, \s_new, \XenophonePartConv, -1, 1, 0, \in,0,\out,0,\bufnumL,2,\bufnumR,3,\amp,0.3]);


		//^score;

	}




	//alien language generator

	//grammar as object, action, then qualifiers based on time/tense, scent, politeness, caste etc
	//simply make words, then related variants for qualifiers (either common variation pattern or exceptions)
	//actually generalise notion of word type too
	//more used words are shorter in general
	document {|path|
		var w;
		var numletters = rrand(4,26); //100
		var numwords = rrand(500,10000);
		var numtenses = rrand(3,20); //every word has multiple variations
		var letters;
		var words;
		var numtypesofword = rrand(2,10);
		//don't have to be equal size
		var weights = ({exprand(0.01,1.0)}!numtypesofword).normalizeSum;
		//var objectwords, actionwords, connectivewords;
		var wordcollections; //types of word, object, action, connective etc
		var grammar;
		var arraytostring;
		var boundaries;
		var tensevariants; // = {{}!(rrand(1,5))}!numtenses;
		var utterences;
		var font;


		arraytostring = {|array|  array.collect({|x| {x.asInteger.asAscii}.try ? "" }).join};

		letters = (21..127).scramble.copyRange(0,numletters-1); //Array.rand(numletters,21,127)

		words = { arraytostring.({letters.choose}!( rrand(1,rrand(1,10)) ) ) }!numwords;
		tensevariants = {  { arraytostring.({letters.choose}!( rrand(1,rrand(1,3)) ) ) }!(rrand(1,10)); }!numtenses;

		//letters.postln;

		words = words.asSet.asArray; //remove duplicates

		words = words.scramble;

		//now include variants

		//could also allow devoiations from pattern
		words = words.collect{|wordnow|  {|i| wordnow ++ (tensevariants[i].choose); }!numtenses };

		//~words = words;

		numwords = words.size;

		//[\here, numwords, weights, (numwords * weights).asInteger, ((numwords * weights).asInteger.integrate)].postln;
		//[ here, 4022, [ 0.041836902156993, 0.39264275977462, 0.56552033806839 ], [ 168, 1579, 2274 ], [ 168, 1747, 4021 ] ]



		boundaries = [0] ++ (((numwords * weights).asInteger).max(1).integrate);

		//~boundaries = boundaries;

		//[2,3,16].integrate

		wordcollections = weights.collect{|val,i| var start = boundaries[i]; var end = boundaries[i+1];


			//[\wordsize, i, words.size, start, end].postln;

			words.copyRange(start,end-1);
		};
		//words.unlace(numtypesofword); //[1,2,3,4,5,6,7].unlace(10)

		//~wordcollections = wordcollections;

		grammar = {{weights.windex}!(rrand(2,20)) }!(rrand(2,200));

		//[0.7,0.3].windex

		//~grammar = grammar;

		//10 sentences
		utterences = {|j|


			var grammarnow = grammar.choose;
			var tensenow = numtenses.rand;
			var sentencenow = grammarnow.collect{|index|  (wordcollections[index].choose)[tensenow]};
			var sentencestring = "";

			sentencenow.do{|word| sentencestring = sentencestring + word};
			//"".postln;

			sentencestring
		}!rrand(30,300);


		//display into Pen font picture, save to PDF?

		font =  Font( Font.availableFonts.choose, rrand(8,24), 0.2.coin, 0.2.coin);


		{
			//Pen.font = font;
			var matrix = [1,[0,0.2.rand,rrand(0.1,3.0)].choose, [0,0.2.rand,rrand(0.1,3.0)].choose, 1, 0, 0];
			var bckcol = Color.rand;

			bckcol.alpha = 1.0.rand;

			w = Window("alien culture",Rect(100,100,1000,1000)).front;


			w.view.background_(Color.rand);
			//StaticText(w, Rect(0,0,200,20))
			//  .string_(" Change Line Cap & Join Styles: ");

			w.drawFunc = {
				Pen.strokeColor = Color.rand;

				if(0.5.coin) {
					//Pen.rotate(angle: [0,05pi.rand].choose, x: 500, y: 500);
					Pen.rotate(angle: 0.5pi, x: 500, y: 500);

				};

				Pen.width_(rrand(0.5,3.0));

				if(0.2.coin) {
					Pen.matrix = matrix;
				};


				"XENOTEXT TRANSMISSION".postln;

				utterences.do {|stringnow,j|

					Pen.stringAtPoint(stringnow,10@(30*j),font,Color.black);

					stringnow.postcs;

				};

				"XENOTEXT TRANSMISSION OVER".postln;

				Pen.stroke;
			};
			w.refresh;

			if(path.notNil) {
				{
					var image;
					0.5.wait;
					image = Image.fromWindow(w);

					//process image?

					image.write((path??{"~/Desktop/xenodoc.jpg"}).standardizePath,"jpg");

					0.5.wait;

					w.close;

				}.fork(AppClock)
			};

		}.defer;



	}



}