+ Xenophone {

	//SynthDef sources
	//Quarks.gui
	//SCLOrkSynths.gui;

	*initSynthDefs {|numChannels=2|

		var s = Server.default;


		// this will record to the disk
		SynthDef(\Xenophonediskout, {arg bufnum;
			DiskOut.ar(bufnum, In.ar(0,numChannels));
		}).store;


		//improve bass end compression: could roll own compressor using Amplitude as here:
		//https://scsynth.org/t/multiband-compressor/3753/4

		SynthDef(\XenophoneMasterFX,{arg in=0;
			var input, process;
			var good;
			var lowend, therest;

			input= In.ar(0,2);

			//cut below 43 Hz to try to reduce feedback issues and spectral clutter (freq at 60 for this)
			// CheckBadValues anti inf/nan/denormal routine?
			//good = input * BinaryOpUGen('==', CheckBadValues.ar(input, 0, 0), 0);

			good = Sanitize.ar(input);

			//-6.dbamp
			//BLowShelf.ar(good,60,1.0, -40)
			process = Limiter.ar(LeakDC.ar(good)*0.5,0.99,0.01).clip(-1.0,1.0);

			//cascade for valid crossover filter https://scsynth.org/t/multiband-compressor/3753/4
			//lowend = LPF.ar(LPF.ar(process,300),300);
			//therest = process - lowend;

			//imaging flat for bass end, more compression on bass
			//process = Pan2.ar(Limiter.ar(Mix(Compander.ar(lowend,lowend,0.1,1,1/9,mul:5))),0.0)+DelayC.ar(therest,0.01,0.01);

			//Limiter.ar(In.ar(0,numChannels),0.99,0.01)
			ReplaceOut.ar(0,process)
			//ReplaceOut.ar(0,Limiter.ar(process))
		}).store;


		15.do{|i|

			var numresonances = rrand(4,11); //7
			var numharmonics = rrand(5,20);
			var b = [0,exprand(1,1.05)-1].choose;
			var stretched;

			SynthDef(\Xenophonepercussion ++ i,{|amp = 0.1 pan = 0.0 attacktime = 0.001 sustaintime = 0.1 releasetime = 0.5|
				var filterdata = `[Array.rand(numresonances,70,1000),Array.rand(numresonances,0.01,0.2),Array.exprand(numresonances,0.05,1)];
				var freqdata = `[Array.rand(numresonances,70,1000),Array.rand(numresonances,0.01,0.2),Array.rand(numresonances,0.0,1.0)];
				var source, filter;

				//Decay.ar(Impulse.ar(4), 0.03, ClipNoise.ar(0.01))
				source = Klang.ar(freqdata) * Decay.ar(Impulse.ar(0), 0.03, ClipNoise.ar(0.01));

				filter = Klank.ar(filterdata,source) * EnvGen.kr(Env([0,1,1,0],[attacktime,sustaintime, releasetime]),doneAction:2)*amp*10;

				OffsetOut.ar(0,Pan2.ar(filter, pan))
			}).store;



			//stretched harmonics: f0 * n * sqrt(1 + (B*n.squared))

			stretched = {|i|  var n = i+1; n * ((1+(b*((n-1).squared))).sqrt)}!numharmonics;

			SynthDef(\Xenophonetone ++ i,{|freq = 440 amp = 0.1 pan=0.0 attacktime = 0.01 sustaintime = 0.5 releasetime = 0.5|
				var filterdata = `[Array.rand(7,70,1000),Array.rand(7,0.01,0.2),Array.exprand(7,0.05,1)];
				//var freqdata = `[Array.rand(7,70,1000),Array.rand(7,0.01,0.2),Array.rand(7,0.0,1.0)];
				var source, filter;

				//Decay.ar(Impulse.ar(4), 0.03, ClipNoise.ar(0.01))
				//source = Klang.ar(freqdata, freqscale:freq) * Decay.ar(Impulse.ar(0), 0.03, ClipNoise.ar(0.01));

				source = (Mix(SinOsc.ar(stretched * freq))/numharmonics)*amp; //Blip.ar(freq,20);

				filter = Klank.ar(filterdata,source) * EnvGen.kr(Env([0,1,1,0],[attacktime,sustaintime,releasetime]),doneAction:2);

				OffsetOut.ar(0,Pan2.ar(filter,pan))
			}).store;


			//another set, FM based
			SynthDef(\Xenophonetone ++ (i+15),{|freq = 440 amp = 0.1 pan=0.0 attacktime = 0.01 sustaintime = 0.5 releasetime = 0.5|
				var source, filter;
				var startmodfreq = exprand(0.1,100);
				var endmodfreq = exprand(0.1,100);
				var startmodindex = rrand(0.01,10.0);
				var endmodindex = rrand(0.01,10.0);

				if(0.3.coin) {endmodfreq = startmodfreq;};
				if(0.3.coin) {endmodindex = startmodindex;};

				source = PMOsc.ar(freq, XLine.kr(startmodfreq,endmodfreq, exprand(0.01,1.0)), XLine.kr(startmodindex,endmodindex, exprand(0.01,1.0)), 2pi.rand)*amp;

				filter = source * EnvGen.kr(Env([0,1,1,0],[attacktime,sustaintime,releasetime]),doneAction:2);

				OffsetOut.ar(0,Pan2.ar(filter,pan))
			}).store;


		};




		//see also wrapping example in wrap help
		[
			[\FX1,{|in, spb|

				CombC.ar(in,spb, spb*(Rand(1.4)*0.25)+0.001,Rand(1,10))

			}],
			[\FX2,{|in, spb|

				FreeVerb.ar(in, 1, Rand(0.5, 0.98), Rand(0.2,0.7));
			}],
			[\FX3,{|in, spb|

				DelayC.ar(in, spb, spb*(Rand(1.4)*0.25));
			}],
			[\FX4,{|in, spb|

				//swing
				DelayC.ar(in, spb*0.38, spb*Rand(0.32,0.38));

			}],
			[\FX5,{|in, spb, param|

				var n = 10;

				//chorusing
				LeakDC.ar(Mix.fill(n, {
					DelayC.ar(in, 0.03, LFNoise1.kr(Rand(5.0,10.0),0.01*param,0.02) )
				}));

			}],
			[\FX6,{|in, spb, param|

				//phasing

				AllpassN.ar(in,0.02,SinOsc.kr(param,0,0.01,0.01)); //max delay of 20msec


			}],
			[\FX7,{|in, spb, param|

				//flanging
				var input = LeakDC.ar(in)+LocalIn.ar(1);
				var effect;

				effect = LeakDC.ar(DelayN.ar(input,0.02,SinOsc.kr(param,0,0.005,0.005))); //max delay of 20msec
				LocalOut.ar(0.995*Rand(0.05,0.15)*effect);

			}],


		].do{|data|
			var name = \Xenophone ++ data[0];

			SynthDef(name, {arg in=16, out=0, spb=0.5, gate=1, amp=0.3,param = 0.1;

				//var env = EnvGen.ar(Env([0,1,1,0],[fadein,dur-fadein-fadeout,fadeout],[envslopein,1,envslopeout]),doneAction:2);
				var env = EnvGen.ar(Env.asr(0.01,1,1),gate, doneAction:2);

				in  = In.ar(in,2);

				Out.ar(out,SynthDef.wrap(data[1],nil,[in,spb,param])*amp);
			}).store;

		};


		SynthDef(\XenophonePartConv, {arg in=16, out=0, bufnumL=0,bufnumR=0, gate=1, amp=0.3;

			var env = EnvGen.ar(Env.asr(0.01,1,1),gate, doneAction:2);
			var leftconv,rightconv;

			in  = In.ar(in,2);

			leftconv = PartConv.ar(in[0],2048,bufnumL);
			rightconv = PartConv.ar(in[1],2048,bufnumR);

			Out.ar(out,[leftconv,rightconv]*env*amp)

		}).store;



	}



}
