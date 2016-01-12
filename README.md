# Tombs - The Card Game
Original card game

Tombs is a 2 to 4 players card game playable on a 3x3 board.
Players draw from the same deck consisted of 4 RPG-themed character cards.

Classes:

Blade (~ warrior):
- Threatens: horizontal and vertical adjacent tiles.
- Can be placed on an empty unthreatened tile.
- Creates tombstones from enemy units in its threat zones. Each tombstone counts as a kill.

Magus (~ mage):
- Threatens: diagonal tiles.
- Can be placed on an empty unthreatened tile.
- Creates tombstones from enemy units in its threat zones. Each tombstone counts as a kill.

Ombra (~ rogue):
- Threatens: its own tile.
- Can be placed on an empty unthreatened tile.
- Can also be placed directly on top of an enemy unit, as long as it is not threatened afterwards.
- The enemy unit is therefore removed, but it does not count as a kill.

Thorn (~ priest):
- Threatens: nothing.
- Can be placed on an empty untreatened tile.
- Can also be activated if there is no enemy thorn in adjacent tiles.

Activated Thorn*:
- Can be placed on a tombstone while removing an adjacent enemy unit, as long as it is not threatened afterwards.
- The tombstone is therefore removed as well. This does not count as a kill.

Turn actions:
- Each player starts with 2 cards. Draw one everytime it is your turn (unless there are no cards left in the deck).
- Play one character card unless there are no moves possible (you are stuck), in which case you discard a card.
- The first player to go cannot play on the middle tile.
- Players cannot make moves that kill or remove targets in the first x total turns of the game.
   - 2 players: x = 3
   - 3 players: x = 2
   - 4 players: x = 3

Remarks:
- A character will never kill another character of the same class (because of placement restrictions).
- No friendly fire.
- You can play cards on a tile threatened by your own units. This actually adds a layer of protection!

Win conditions:
- Field lock: you win if all the players besides yourself are stuck for one full rotation of turns.
- Kill count: first player to reach the kill goal wins.
   - 2 players: 8 kills
   - 3 players: 7 kills
   - 4 players: 6 kills
- Unit count: when all cards are played (no more cards in deck nor hands):
   - Player with the most units on the board wins.
   - In case of a tie, the tied player with the most kills wins.
   - If it's still a tie, then it ends with a tie.

A 4 players game can be played with 2 teams of 2, although no in-game rule changes.
