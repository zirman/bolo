package client

import common.SPRITE_SHEET_WIDTH
import client.math.pi

enum class Sprite(val int: Int) {
    TankBoat0(spriteInd(col = 0, row = 0)),
    TankBoat1(spriteInd(col = 1, row = 0)),
    TankBoat2(spriteInd(col = 2, row = 0)),
    TankBoat3(spriteInd(col = 3, row = 0)),
    TankBoat4(spriteInd(col = 4, row = 0)),
    TankBoat5(spriteInd(col = 5, row = 0)),
    TankBoat6(spriteInd(col = 6, row = 0)),
    TankBoat7(spriteInd(col = 7, row = 0)),
    TankBoat8(spriteInd(col = 8, row = 0)),
    TankBoat9(spriteInd(col = 9, row = 0)),
    TankBoat10(spriteInd(col = 10, row = 0)),
    TankBoat11(spriteInd(col = 11, row = 0)),
    TankBoat12(spriteInd(col = 12, row = 0)),
    TankBoat13(spriteInd(col = 13, row = 0)),
    TankBoat14(spriteInd(col = 14, row = 0)),
    TankBoat15(spriteInd(col = 15, row = 0)),
    Tank0(spriteInd(col = 0, row = 1)),
    Tank1(spriteInd(col = 1, row = 1)),
    Tank2(spriteInd(col = 2, row = 1)),
    Tank3(spriteInd(col = 3, row = 1)),
    Tank4(spriteInd(col = 4, row = 1)),
    Tank5(spriteInd(col = 5, row = 1)),
    Tank6(spriteInd(col = 6, row = 1)),
    Tank7(spriteInd(col = 7, row = 1)),
    Tank8(spriteInd(col = 8, row = 1)),
    Tank9(spriteInd(col = 9, row = 1)),
    Tank10(spriteInd(col = 10, row = 1)),
    Tank11(spriteInd(col = 11, row = 1)),
    Tank12(spriteInd(col = 12, row = 1)),
    Tank13(spriteInd(col = 13, row = 1)),
    Tank14(spriteInd(col = 14, row = 1)),
    Tank15(spriteInd(col = 15, row = 1)),
    TankFriendlyBoat0(spriteInd(col = 0, row = 2)),
    TankFriendlyBoat1(spriteInd(col = 1, row = 2)),
    TankFriendlyBoat2(spriteInd(col = 2, row = 2)),
    TankFriendlyBoat3(spriteInd(col = 3, row = 2)),
    TankFriendlyBoat4(spriteInd(col = 4, row = 2)),
    TankFriendlyBoat5(spriteInd(col = 5, row = 2)),
    TankFriendlyBoat6(spriteInd(col = 6, row = 2)),
    TankFriendlyBoat7(spriteInd(col = 7, row = 2)),
    TankFriendlyBoat8(spriteInd(col = 8, row = 2)),
    TankFriendlyBoat9(spriteInd(col = 9, row = 2)),
    TankFriendlyBoat10(spriteInd(col = 10, row = 2)),
    TankFriendlyBoat11(spriteInd(col = 11, row = 2)),
    TankFriendlyBoat12(spriteInd(col = 12, row = 2)),
    TankFriendlyBoat13(spriteInd(col = 13, row = 2)),
    TankFriendlyBoat14(spriteInd(col = 14, row = 2)),
    TankFriendlyBoat15(spriteInd(col = 15, row = 2)),
    TankFriendly0(spriteInd(col = 0, row = 3)),
    TankFriendly1(spriteInd(col = 1, row = 3)),
    TankFriendly2(spriteInd(col = 2, row = 3)),
    TankFriendly3(spriteInd(col = 3, row = 3)),
    TankFriendly4(spriteInd(col = 4, row = 3)),
    TankFriendly5(spriteInd(col = 5, row = 3)),
    TankFriendly6(spriteInd(col = 6, row = 3)),
    TankFriendly7(spriteInd(col = 7, row = 3)),
    TankFriendly8(spriteInd(col = 8, row = 3)),
    TankFriendly9(spriteInd(col = 9, row = 3)),
    TankFriendly10(spriteInd(col = 10, row = 3)),
    TankFriendly11(spriteInd(col = 11, row = 3)),
    TankFriendly12(spriteInd(col = 12, row = 3)),
    TankFriendly13(spriteInd(col = 13, row = 3)),
    TankFriendly14(spriteInd(col = 14, row = 3)),
    TankFriendly15(spriteInd(col = 15, row = 3)),
    TankEnemyBoat0(spriteInd(col = 0, row = 4)),
    TankEnemyBoat1(spriteInd(col = 1, row = 4)),
    TankEnemyBoat2(spriteInd(col = 2, row = 4)),
    TankEnemyBoat3(spriteInd(col = 3, row = 4)),
    TankEnemyBoat4(spriteInd(col = 4, row = 4)),
    TankEnemyBoat5(spriteInd(col = 5, row = 4)),
    TankEnemyBoat6(spriteInd(col = 6, row = 4)),
    TankEnemyBoat7(spriteInd(col = 7, row = 4)),
    TankEnemyBoat8(spriteInd(col = 8, row = 4)),
    TankEnemyBoat9(spriteInd(col = 9, row = 4)),
    TankEnemyBoat10(spriteInd(col = 10, row = 4)),
    TankEnemyBoat11(spriteInd(col = 11, row = 4)),
    TankEnemyBoat12(spriteInd(col = 12, row = 4)),
    TankEnemyBoat13(spriteInd(col = 13, row = 4)),
    TankEnemyBoat14(spriteInd(col = 14, row = 4)),
    TankEnemyBoat15(spriteInd(col = 15, row = 4)),
    TankEnemy0(spriteInd(col = 0, row = 5)),
    TankEnemy1(spriteInd(col = 1, row = 5)),
    TankEnemy2(spriteInd(col = 2, row = 5)),
    TankEnemy3(spriteInd(col = 3, row = 5)),
    TankEnemy4(spriteInd(col = 4, row = 5)),
    TankEnemy5(spriteInd(col = 5, row = 5)),
    TankEnemy6(spriteInd(col = 6, row = 5)),
    TankEnemy7(spriteInd(col = 7, row = 5)),
    TankEnemy8(spriteInd(col = 8, row = 5)),
    TankEnemy9(spriteInd(col = 9, row = 5)),
    TankEnemy10(spriteInd(col = 10, row = 5)),
    TankEnemy11(spriteInd(col = 11, row = 5)),
    TankEnemy12(spriteInd(col = 12, row = 5)),
    TankEnemy13(spriteInd(col = 13, row = 5)),
    TankEnemy14(spriteInd(col = 14, row = 5)),
    TankEnemy15(spriteInd(col = 15, row = 5)),
    Shell0(spriteInd(col = 0, row = 6)),
    Shell1(spriteInd(col = 1, row = 6)),
    Shell2(spriteInd(col = 2, row = 6)),
    Shell3(spriteInd(col = 3, row = 6)),
    Shell4(spriteInd(col = 4, row = 6)),
    Shell5(spriteInd(col = 5, row = 6)),
    Shell6(spriteInd(col = 6, row = 6)),
    Shell7(spriteInd(col = 7, row = 6)),
    Shell8(spriteInd(col = 8, row = 6)),
    Shell9(spriteInd(col = 9, row = 6)),
    Shell10(spriteInd(col = 10, row = 6)),
    Shell11(spriteInd(col = 11, row = 6)),
    Shell12(spriteInd(col = 12, row = 6)),
    Shell13(spriteInd(col = 13, row = 6)),
    Shell14(spriteInd(col = 14, row = 6)),
    Shell15(spriteInd(col = 15, row = 6)),
    Explosion0(spriteInd(col = 0, row = 7)),
    Explosion1(spriteInd(col = 1, row = 7)),
    Explosion2(spriteInd(col = 2, row = 7)),
    Explosion3(spriteInd(col = 3, row = 7)),
    Explosion4(spriteInd(col = 4, row = 7)),
    Explosion5(spriteInd(col = 5, row = 7)),
    Lgm0(spriteInd(col = 6, row = 7)),
    Lgm1(spriteInd(col = 7, row = 7)),
    Parachute(spriteInd(col = 8, row = 7)),
    Reticule(spriteInd(col = 9, row = 7)),
    Cursor(spriteInd(col = 10, row = 7));

    fun withBearing(bearing: Float): Sprite =
        entries[(ordinal + ((bearing + (Float.pi * (1.0 / 16.0))) * (8.0 / Float.pi)).toInt().mod(16))]
}

private fun spriteInd(col: Int, row: Int): Int = (SPRITE_SHEET_WIDTH * row) + col
