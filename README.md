# Xenophone
by [Nick Collins](http://composerprogrammer.com/index.html)

## License
For [SuperCollider 3](https://supercollider.github.io/), all code under the usual GNU GPL 3 license, see the COPYING file.

## Installation
Put this folder in your extensions directory (location will depend on your platform, run Platform.userExtensionDir in SuperCollider). If you need further hints on how to do that, see the [Using Extensions] help file in SuperCollider.

If you are on Mac, the code will use the SCImage class to flash up an alien language document with each run (image copy saved to the SuperCollider temp directory). Example language text will also go to the post window on all platforms.

The project contains a subfolder, "IR", which contains some example impulse responses from open internet sources (links in the Xenophoneclient.scd file, files renamed for convenience). If anyone has an issue with reuse of these impulse responses here, I am happy to remove them as required. Impulse responses can be mono or stereo but 44.1KHz is typically expected. Feel free to substitute any sound files, conventional impulse response or not, into your local copy of this folder.

## About

**Xenomusic/Xenophones**

To imagine a more distant alien music worthy of strange new worlds, rather than cliched sci-fi media music overly influenced by Hollywood and TV (Summers 2013), the foundations of music as a time-based art form can be abstracted in a new way. Unfamiliar exoplanets and unknown xenobiology may lead to music transmitted in the vibrations of other elements and materials, intended for different auditory conditions and physiological apparatus, even at different cognitive rates, all impacting on the perceptibility to human ears of xenomusical information. Nonetheless, we proceed pragmatically, dropping certain preconditions of standard human music theory whilst keeping (for the moment) frequencies within human hearing range and at rhythmic rates to some degree perceivable at human perceptual rates favouring a 2-3 second perceptual present. Other worlds can have rather different acoustic situations, for example with higher fluid loading effects and faster speeds of sound in denser atmospheres impacting on formants and vibrational fundamentals (as for example happens on the surface of Venus, where if you could speak without immediate dissolution amidst the sulphuric acid you would have heavier vocal folds for a deeper source tone but higher formants from the higher speed of sound for a child's vocal timbre, see Leighton and Petculescu 2009a, 2009b). We don't diverge acoustic equations too far, not worrying about propagation distances (with a sparse Martian atmosphere sound can disappear within 10 metres), or alien geometries affecting impulse responses leading to radically different reverbs, but utilise potential physical universals, such as the stiff string equation (Fletcher and Rossing 2012). If necessary, we can posit that some translation through sonification techniques (Zanella et al. 2022) has taken place to enable hearing the alien music.

The project here consists of two parts; 1) a music generator founded in a model of time and frequency content based in shorter or longer spans of working memory than a human norm (though its output is certainly interpretable by human ears) 2) a highly abstracted alien language generator used to make 'written' documentation of the musical tradition; the symbols are here translated from alien symbols to ascii to give some chance of human appreciation, but the grammar and vocabulary is highly unusual (again founded in different orders of working memory allowing for smaller or larger numbers of units and grammatical constructs). The implementation uses the SuperCollider audio synthesis programming language for both. We provide a few more details of each of these:

### The xenophone music generator

- Choose a memory size M from 2 to 30 items (humans would be 7+-2).
- Choose a set of M differently perceptible durations
- Make a metrical cycle, using selection from the basic durations
- Set up an importance grid over the cycle with both positive (favour) and negative (avoid) ratings at each non-isochronous timestep
- Choose a number of voices from 2 to 15 (level of polyphony)
- Generate rhythms for different voices (with the possibility to invert the importance grid). Importance gets downweighted for grid positions the more voices choose that position already, to promote counterpoint. Each run through the cycle, subtle variations are possible
- Choose a number of discernible frequencies (based on memory size M)
- Choose some frequency subsets for different sections of the piece
- Choose pitch selection principles, including options for serialism and random selection
- Generate music per cycle, utilising the rhythms and pitch selection criteria for each voice

Synthesis is via a set of resonant filterbanks (with modes randomly picked) for more percussive sounds, and stretched string (stiff string equation) or frequency modulation algorithms for pitched voices (aliens making electronic music would surely discover such mathematical principles too, regardless of their home world acoustics). Voices can be percussive or tonal. Global effects are applied, including the potential for convolution reverb with an impulse response selected from a catalogue supplied (readily changed in the codebase).

### The xenotext language generator

To create accompanying documentation of the xenomusical tradition:

- Make a set of letters (4 to 50 different symbols)
- Make a set of words
- Choose a number of 'types' of word (with weightings for usage)
- Create a grammar setting out permissible sentence constructions (not currently a recursive grammar)
- Choose a number of 'tenses', giving modifiers to words
- Generate sentences according to a selected grammar and tense, with words from the appropriate type at each position, modified by the current tense


### Xenomusicology

This work is an initial step into generative xenomusicology (alien analysis by synthesis), but many future developments can be envisaged. The current project is a human imagining possible abstractions of music that are sufficiently mathematical to also be imagined on other worlds as musical constructive starting points. We can imagine an alien musicologist trying to imagine human music, or an alien imagining a human trying to make alien music, and deeper levels yet. We might proceed from the physics of an exoplanet (informed by the catalogue of exoplanets) to its geography and acoustics, then to its xenobiology, musical species, and xenoculture; and the modelled culture may be a far richer hubbub of ideas, akin to the mass production of music on Earth, just within as yet unknown music theories.

### References

Eklund, R. and Lindström, A. 1998. How To Handle “Foreign” Sounds in Swedish Text-to-Speech Conversion: Approaching the ‘Xenophone’ Problem. Proceedings of The International Conference on Spoken Language Processing, 30 November–5 December 1998, Sydney, Australia. Paper 514, vol. 7, pp. 2831–2834.

Fletcher, N.H. and Rossing, T.D., 2012. The physics of musical instruments. Springer Science & Business Media.

Leighton, T.G. and Petculescu, A., 2009a. The sound of music and voices in space. Part 1: Theory. Acoustics Today, 5(3): 17-26.

Leighton, T.G. and Petculescu, A., 2009b. The sound of music and voices in space. Part 2: Modeling and simulation. Acoustics Today 5(3): 27-29.

Summers, T. 2013. Star Trek and the Musical Depiction of the Alien Other. Music, Sound, and the Moving Image
7(1): 19-52

Zanella, A., Harrison, C. J., Lenzi, S., Cooke, J., Damsma, P., and Fleming, S. W. 2022. Sonification and sound design for astronomy research, education and public engagement. Nature Astronomy 6(11): 1241–1248.
