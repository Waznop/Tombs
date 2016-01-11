# Tombs - The Card Game
Original card game

Tombs is a 2 to 4 players card game playable on a 3x3 board.
Players draw from the same deck consisted of 4 RPG-themed character cards.

Classes:

Blade:
Threatens: horizontal and vertical adjacent tiles.
Can be placed on an empty unthreatened tile.
Creates tombstones from enemy units in its threat zones.

Magus:
Threatens: diagonal tiles.
Can be placed on an empty unthreatened tile.
Creates tombstones from enemy units in its threat zones.

Ombra:
Threatens: its own tile
Can be placed on an empty unthreatened tile.
Can also be placed directly on top of an enemy unit, as long as it is not threatened afterwards.
The enemy unit is therefore removed, but it does not count as a kill.

Thorn:
Threatens: nothing
Can be placed on an empty untreatened tile.
Can also be activated if there is no thorn in adjacent tiles.

Activated Thorn*:
Can be placed on a tombstone while removing an adjacent enemy unit, as long as it is not threatened afterwards.
The tombstone is therefore removed as well. This does not count as a kill.

Turn actions:
- Every player starts with 2 cards. Draw one everytime it is your turn (unless there are no cards left in the deck).
- Play one character card unless there are no moves possible (you are stuck), in which case you discard a card.

Remarks:
- A character will never kill another character of the same class
- No friendly fire
- You can play cards on a tile threatened by your own units. This actually adds a layer of protection!

Win conditions:
1. Field lock: you win if every player besides yourself is stuck for one rotation of turns
2. Kill count: first player to reach the kill goal wins
   - 2 players: 8 kills
   - 3 players: 7 kills
   - 4 players: 6 kills
3. Unit count: when all cards are played (no more cards in deck nor hands):
   - player with most units on the board wins
   - in case of a tie, player with most kills wins
   - if it's still a tie, then it's a tie
