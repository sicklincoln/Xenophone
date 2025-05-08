//generative system to create imagined alien music, or even alien music imagined by aliens observing Earth from afar and imagining Earth music

+ Xenophone {


	//alien language generator

	//grammar as object, action, then qualifiers based on time/tense, scent, politeness, caste etc
	//simply make words, then related variants for qualifiers (either common variation pattern or exceptions)
	//actually generalise notion of word type too
	//more used words are shorter in general
	video {|path, numframes = 300, width = 1080, height = 1920, framenumoffset=0|
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
		var utterfunc;
		var utterences;
		var font;
		var widthhalfway = width.div(2);
		var heighthalfway = height.div(2);

		var bckcol = Color.rand;
		//Pen.font = font;
		var matrix = [1,[0,0.2.rand,rrand(0.1,3.0)].choose, [0,0.2.rand,rrand(0.1,3.0)].choose, 1, 0, 0];
		var rotationflag = 0.5.coin;
		var penwidth = rrand(0.5,3.0);
		var matrixflag = 0.2.coin;
		var framecounter = 0;
		var drawrate = rrand(0.25,4.0);
		var penoffset = if(rotationflag, rrand(heighthalfway.neg,heighthalfway), rrand(10,widthhalfway));
		var rightleft = 0.5.coin;
		var changechance = [1.0,0.05,exprand(0.01,0.5)].choose; //, 0.01,rrand(0.001,0.1)
		var setuplanguage;

		bckcol.alpha = 1.0.rand;


		setuplanguage = { arraytostring = {|array|  array.collect({|x| {x.asInteger.asAscii}.try ? "" }).join};

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
		utterfunc = {|j|

			var grammarnow = grammar.choose;
			var tensenow = numtenses.rand;
			var sentencenow = grammarnow.collect{|index|  (wordcollections[index].choose)[tensenow]};
			var sentencestring = "";

			sentencenow.do{|word| sentencestring = sentencestring + word};
			//"".postln;

			sentencestring
		};

		};

		setuplanguage.();

		utterences = {|j| utterfunc.(j)}!rrand(30,300);


		//display into Pen font picture, save to PDF?

		font =  Font( Font.availableFonts.choose, rrand(8,24), 0.2.coin, 0.2.coin);



		[\makewindow, \width, width, \height, height].postln;
		//tik tok size
		w = Window("alien culture",Rect(100,100,width,height),false,false).front;


		w.view.background_(Color.rand);
		//StaticText(w, Rect(0,0,200,20))
		//  .string_(" Change Line Cap & Join Styles: ");

		w.drawFunc = Routine {

				Pen.strokeColor = Color.rand;


			numframes.do {|framenum|

				[\xenovideoframe, framenum ].postln;

				if(0.1.coin) {

					changechance = [1.0,0.05,exprand(0.01,0.5),rrand(0.001,0.1)].choose;

				};


				if(changechance.coin) {

					utterences = {|j| utterfunc.(j)}!rrand(30,300);

					framecounter = rrand(0,50);

					drawrate = rrand(0.05,6.0);

					penoffset = if(rotationflag, rrand(heighthalfway.neg,heighthalfway), rrand(10,widthhalfway));


					rightleft = 0.5.coin;

					if(0.2.coin) {
					Pen.strokeColor = Color.rand;
					w.view.background_(Color.rand);
					};

					//change language
					if(0.2.coin) {

						setuplanguage.();
						rotationflag = 0.5.coin;

					};

					//Pen.strokeColor = Color.rand;
				};



				if(rotationflag) {
					//Pen.rotate(angle: [0,05pi.rand].choose, x: 500, y: 500);
					Pen.rotate(angle: 0.5pi, x: widthhalfway, y: heighthalfway);

				};


				if(rightleft) {
					Pen.rotate(angle: pi, x: widthhalfway, y: heighthalfway);

				};

				Pen.width_(penwidth);

				if(matrixflag) {
					Pen.matrix = matrix;
				};

				utterences.do {|stringnow,j|

					Pen.stringAtPoint(stringnow.copyRange(0,(framecounter*drawrate).roundUp.asInteger),penoffset@(30*j),font,Color.black);

					//stringnow.postcs;

				};

				Pen.stroke;

				0.yield;

			}

		};

		{

			var image;

			var maxpadding = (numframes+framenumoffset-1).asString.size - 1;
			//"10000".size

			numframes.do {|fnum|

				var framenum = fnum + framenumoffset;
				var framestring = framenum.asString;
				var padding;
				var tempstring;

				//processing file naming convention requires this (assumes numframes <10000)
				//pad framestring
				padding = framestring.size - maxpadding;

				if( padding > 0) {

					tempstring = "";
					padding.do{tempstring = tempstring + "0";};

					framestring = tempstring ++ framestring;

				};

				//jump color, pen size, perturb matrix etc

				w.refresh;

				0.1.wait;

				image = Image.fromWindow(w);

				//process image?

				image.write(((path??{"~/Desktop/output/"})++"xenoframe"++framestring++".png").standardizePath,"png");

				framecounter = framecounter + 1;

				0.1.wait;

			};

			w.close;

		}.fork(AppClock);




	}



}