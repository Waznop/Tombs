# Tombs - The Card Game
Original card game

Tombs is a 2 to 4 players card game playable on a 3x3 board.
Players draw from the same deck consisted of 4 RPG-themed character cards.

Classes:

Blade:
Threatens: horizontal and vertical adjacent tiles.
Can be placed on an empty unthreatened tile.
Creates tombstones from enemy units in its threatened zones.

Magus:
Threatens: diagonal tiles.
Can be placed on an empty unthreatened tile.
Creates tombstones from enemy units in its threatened zones.

Ombra:
Threatens: its own tile
Can be placed on an empty unthreatened tile.
Can also be placed directly on top of an enemy unit, as long as it is not threatened afterwards.
The enemy unit is therefore removed, but it does not count as a kill.

Thorn:
Threatens: nothing
Can be placed on an empty untreatened tile.
Can also be activated if there is no thorn in adjacent tiles.

Activation:
Can be placed on a tombstone while removing an adjacent enemy unit, as long as it is not threatened afterwards.
The tombstone is therefore removed as well. This does not count as a kill.

*You can play cards on a tile threatened by your own units. This actually adds a layer of protection!

More rules:
- Every player starts with 2 cards. Draw one everytime it is your turn.
- Play one character card unless there are no moves possible, in which case you discard a card.

As a result, here are some invariants:
- a character will never kill another character of the same class
- no friendly fire

Win conditions:
