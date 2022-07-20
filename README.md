# PokeGear Deck Importer

*"I often find my most fun projects are born from hate." -- Mahatma Gandalf: Marchtember 1th, 2077

PokeGear is a tool allowing the user to dynamically generate custom Pokemon TCG decks for use within Tabletop Simulator without reliance on mods, file sharing, or external downloads. Users can copy a decklist from a source such as a Pokemon TCGO export-to-clipboard and paste it into PokeGear, which will then use a webcrawler to assign and format an image for each card to a custom deck that Tabletop Simulator (TTS) can read. 

## Envisioned End Product

The facetious fake quote probably gave you a decent idea of why I developed this tool. Decker and TTS-Deckbuilder are both great tools that can build Tabletop Simulator custom decks for multiple different card games, and while I have kept possible inclusion of other card games in mind and may add support for others later, these tools had significant issues that seem to be most severe with Pokemon TCG, the one I play and enjoy the most.

Due to these issues, I designed this program from the start with possible solutions for these problems in mind, and I have done what I can to present useful solutions or prevent them entirely.

### Feature Set (Implemented)
  *  Stage-Based Deck Parsing from Most Standard Deck Formats
      *  The Importer attempts to parse input from any deck list format with card entries following the format of [COUNT] [CARD NAME WITH OR WITHOUT SPACES] [3 LETTER SET ABBREVIATION] [SET ID], ie. 2 Bidoof PRC 117
      *  Deck lists from any source should work so long as non-alphanumeric characters serve as line delimeters, for instance how PTCGO adds asterisks before each entry
      *  Verified compatible with PTCGO and LimitlessTCG formats
  *  Automatic Error Reporting
      *  Any card that fails parsing is immediately reported to the user and repeated once parsing is complete, allowing them to know what is missing beforehand
  *  Preemptive Ambiguity Fixes
      *  If the webcrawler encounters a duplicate card, it will verify that they are truly duplicate and automatically pick the latest version if present, breaking the tie for you
      *  Basic Energy cards are manually added to prevent ambiguity, since not all decklists properly specify them in the above template  
