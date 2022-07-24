# PokeGear Deck Importer

"I often find my most fun projects are born from hate." -- Mahatma Gandalf: Marchtember 1th, 2077

PokeGear is a tool allowing the user to dynamically generate custom Pokemon TCG decks for use within Tabletop Simulator without reliance on mods, file sharing, or external downloads. Users can copy a decklist from a source such as a Pokemon TCGO export-to-clipboard and paste it into PokeGear, which will then use a webcrawler to assign and format an image for each card to a custom deck that Tabletop Simulator (TTS) can read. 

## Envisioned End Product

The facetious fake quote probably gave you a decent idea of why I developed this tool. Decker and TTS-Deckbuilder are both great tools that can build Tabletop Simulator custom decks for multiple different card games, and while I have kept possible inclusion of other card games in mind and may add support for others later, these tools had significant issues that seem to be most severe with Pokemon TCG, the one I play and enjoy the most.

Due to these issues, I designed this program from the start with possible solutions for these problems in mind, and I have done what I can to present useful solutions or prevent them entirely.

### Feature Set (Implemented)
  *  Stage-Based Deck Parsing from Most Standard Deck Formats
      *  The Importer attempts to parse input from any deck list format with card entries following the format of [COUNT] [CARD NAME WITH OR WITHOUT SPACES] [3 LETTER SET ABBREVIATION] [SET ID], ie. 2 Bidoof PRC 117
      *  Deck lists from any source should work so long as non-alphanumeric characters serve as line delimeters, for instance how PTCGO adds asterisks before each entry
      *  Verified compatible with PTCGO and LimitlessTCG formats
  *  Automatic Error Reporting (Ongoing Improvements Necessary)
      *  Any card that fails parsing is immediately reported to the user and repeated once parsing is complete, allowing them to know what is missing beforehand
      *  Not all errors are currently caught properly, but most errors will be shown to the user along with what deck entry threw the errors.
  *  Preemptive Ambiguity Fixes
      *  If the webcrawler encounters a duplicate card, it will verify that they are truly duplicate and automatically pick the latest version if present, breaking the tie for you
      *  Basic Energy cards are manually added to prevent ambiguity, since not all decklists properly specify them in the above template  

### Feature Set (Planned)
 *  [Minor] Deck Thumbnails (In-Dev)
      *  The Importer currently does not assign any thumbnails to decks in-game, although this is a simple addition I ought to have in the next version
 *  [Major] Full GUI (In-Dev, Top Priority)
      *  The program, as it stands, is CLI-only at the moment and can't even be run independent of the command line. This is the most important next step for my work on the project, and I am focusing on building a GUI so that users no longer will have to use terminal to run the program.
      *  The UI will entail a text box, help screen, examples, and an options screen to validate your file paths at bear minimum.
      *  Additional features are planned too, which I will go over.
 *  [Medium] Deck Customization Options
      *  GUI Standalone version will, eventually, have options to customize certain aspects about your deck including card backs, basic energy styles, custom thumbnails (apart from the generated thumbnail), creating subfolders in your TTS Saved Objects directory, and potentially a number of experimental features.

## Known Issues
The issues tab holds a few known problems I have encountered so far, but I will review them here too:
 *  Tag Team Pokemon not always properly imported
      *  The illegal character filter in my parser is a little overzealous, and I honestly just forgot that Tag Teams have an ampersand in their name
 *  Older-Generation Promo Cards not always fetched by the webcrawler
      *  Upon testing and researching this problem, it appears that the Black & White sets and earlier did not always use the standard nomenclature for promo cards that the current card sets XY+ use. For instance, the BW Worlds Promo Stadium Card "Tropical Beach" is not caught due to the set code not matching the base-set for the generation. This card uses set code PR-BW instead of PR-BLW as my program would expect given newer set promos.
