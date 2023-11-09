//Client code for Xenophone, an alien music and language generator


g = Xenophone()

g.play //play forever, run again to replace with a new piece

g.play(true, 100.0); //NRT mode, render a piece around 100 seconds long (based on number of cycles fitting)
//by default renders to Platform.defaultTempDir

g.stop //stop any routines in progress


g.universe; //keep playing new planets every 30 seconds to 5 minutes, 'infinite listening' mode


//creates 600 individual png image files, ready to stitch together into a movie (e.g.with Processing Movie Maker)
g.video("/Users/ioi/Desktop/xenovideo/",600,540,960) //defaults to tik tok size for mobile comes out as 1080 by 1920



s.freqscope
s.scope

(
s.recHeaderFormat = "wav";
s.recSampleFormat = "int24";
s.record
)

s.stopRecording













////////////////////////////////tests and planning

//PathName(PathName(Xenophone.filenameSymbol.asString).pathOnly ++ "IR").entries.collect{|val| val.fullPath}.postcs

g.document;

g.document("~/Desktop/hello.jpg")

//further IR responses can be added to the IR folder within the code
PathName(Xenophone.filenameSymbol.asString).pathOnly ++ "IR"

//impulse response source examples:
//http://www.echothief.com/
//https://www.openair.hosted.york.ac.uk/
//https://www.reasonstudios.com/blog/free-reverb-resources-where-to-find-impulse-responses
g.setConvolutionReverb("/data/audio/IR/EchoThiefImpulseResponseLibrary/Venues/SteinmanHall.wav")
g.setConvolutionReverb("/data/audio/IR/EchoThiefImpulseResponseLibrary/Venues/MillsGreekTheater.wav")
g.setConvolutionReverb("/data/audio/IR/AirwindowsImpulses/RoomConcertHall.aiff")
g.setConvolutionReverb("/data/audio/IR/AirwindowsImpulses/OutdoorStadium.aiff")
g.setConvolutionReverb("/data/audio/IR/AirwindowsImpulses/OutdoorStadium.aiff")
g.setConvolutionReverb("/data/audio/IR/skitterstereo.wav")


//DONE: sometimes pan voices over stereo sound stage, or two groups in antiphony, or all together (N groups?)
//DONE occasionally: all percussive, all tonal
//DONE sections have different pitch sets according to sequential or different random selection procedures
//DONE voice specific envelope models; short or long attack, duration for sustain, release time
//DONE common across voices, or disparate
//DONE allow one or more drone voices


//NRT render movie via image frames

//miverb recommended
//https://github.com/v7b1/mi-UGens

//sectionally, swap voice types
//allow transposition (via freq shiift, or some other mechanism)
//pitch subsets available at different times, pitches for harmony vs pitches for melody, repetition of melodic material (sequence restrictions)
//add sense of sections, what is longer term form?
//extra parameters, extra synthesis options, different instruments, more or less sustained tones
//DONE eg aliens know about FM (PhaseMod), etc


