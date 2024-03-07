@file:Suppress("unused")

package client

const val TILE_SHEET_WIDTH: Int = 16
const val TILE_SHEET_HEIGHT: Int = 16
const val TILES_COUNT: Int = TILE_SHEET_WIDTH * TILE_SHEET_HEIGHT

fun imageTileIndex(col: Int, row: Int): Int = (TILE_SHEET_WIDTH * row) + col

enum class ImageTile(val index: Int) {
    Sea0(imageTileIndex(col = 8, row = 4)),
    Sea1(imageTileIndex(col = 9, row = 4)),
    Sea2(imageTileIndex(col = 10, row = 4)),
    Sea3(imageTileIndex(col = 8, row = 5)),
    Sea4(imageTileIndex(col = 9, row = 5)),
    Sea5(imageTileIndex(col = 10, row = 5)),
    Sea6(imageTileIndex(col = 8, row = 6)),
    Sea7(imageTileIndex(col = 9, row = 6)),
    Sea8(imageTileIndex(col = 10, row = 6)),
    SeaMined(imageTileIndex(col = 11, row = 6)),
    Boat0(imageTileIndex(col = 3, row = 13)),
    Boat1(imageTileIndex(col = 4, row = 13)),
    Boat2(imageTileIndex(col = 5, row = 13)),
    Boat3(imageTileIndex(col = 6, row = 13)),
    Boat4(imageTileIndex(col = 7, row = 13)),
    Boat5(imageTileIndex(col = 8, row = 13)),
    Boat6(imageTileIndex(col = 9, row = 13)),
    Boat7(imageTileIndex(col = 10, row = 13)),
    Wall0(imageTileIndex(col = 12, row = 0)),
    Wall1(imageTileIndex(col = 13, row = 0)),
    Wall2(imageTileIndex(col = 14, row = 0)),
    Wall3(imageTileIndex(col = 15, row = 0)),
    Wall4(imageTileIndex(col = 12, row = 1)),
    Wall5(imageTileIndex(col = 13, row = 1)),
    Wall6(imageTileIndex(col = 14, row = 1)),
    Wall7(imageTileIndex(col = 15, row = 1)),
    Wall8(imageTileIndex(col = 12, row = 2)),
    Wall9(imageTileIndex(col = 13, row = 2)),
    Wall10(imageTileIndex(col = 14, row = 2)),
    Wall11(imageTileIndex(col = 15, row = 2)),
    Wall12(imageTileIndex(col = 12, row = 3)),
    Wall13(imageTileIndex(col = 13, row = 3)),
    Wall14(imageTileIndex(col = 14, row = 3)),
    Wall15(imageTileIndex(col = 15, row = 3)),
    Wall16(imageTileIndex(col = 12, row = 4)),
    Wall17(imageTileIndex(col = 13, row = 4)),
    Wall18(imageTileIndex(col = 14, row = 4)),
    Wall19(imageTileIndex(col = 15, row = 4)),
    Wall20(imageTileIndex(col = 12, row = 5)),
    Wall21(imageTileIndex(col = 13, row = 5)),
    Wall22(imageTileIndex(col = 14, row = 5)),
    Wall23(imageTileIndex(col = 15, row = 5)),
    Wall24(imageTileIndex(col = 12, row = 6)),
    Wall25(imageTileIndex(col = 13, row = 6)),
    Wall26(imageTileIndex(col = 14, row = 6)),
    Wall27(imageTileIndex(col = 15, row = 6)),
    Wall28(imageTileIndex(col = 12, row = 7)),
    Wall29(imageTileIndex(col = 13, row = 7)),
    Wall30(imageTileIndex(col = 14, row = 7)),
    Wall31(imageTileIndex(col = 15, row = 7)),
    Wall32(imageTileIndex(col = 12, row = 8)),
    Wall33(imageTileIndex(col = 13, row = 8)),
    Wall34(imageTileIndex(col = 14, row = 8)),
    Wall35(imageTileIndex(col = 15, row = 8)),
    Wall36(imageTileIndex(col = 12, row = 9)),
    Wall37(imageTileIndex(col = 13, row = 9)),
    Wall38(imageTileIndex(col = 14, row = 9)),
    Wall39(imageTileIndex(col = 15, row = 9)),
    Wall40(imageTileIndex(col = 12, row = 10)),
    Wall41(imageTileIndex(col = 13, row = 10)),
    Wall42(imageTileIndex(col = 14, row = 10)),
    Wall43(imageTileIndex(col = 15, row = 10)),
    Wall44(imageTileIndex(col = 12, row = 11)),
    Wall45(imageTileIndex(col = 13, row = 11)),
    Wall46(imageTileIndex(col = 14, row = 11)),
    River0(imageTileIndex(col = 8, row = 0)),
    River1(imageTileIndex(col = 9, row = 0)),
    River2(imageTileIndex(col = 10, row = 0)),
    River3(imageTileIndex(col = 11, row = 0)),
    River4(imageTileIndex(col = 8, row = 1)),
    River5(imageTileIndex(col = 9, row = 1)),
    River6(imageTileIndex(col = 10, row = 1)),
    River7(imageTileIndex(col = 11, row = 1)),
    River8(imageTileIndex(col = 8, row = 2)),
    River9(imageTileIndex(col = 9, row = 2)),
    River10(imageTileIndex(col = 10, row = 2)),
    River11(imageTileIndex(col = 11, row = 2)),
    River12(imageTileIndex(col = 8, row = 3)),
    River13(imageTileIndex(col = 9, row = 3)),
    River14(imageTileIndex(col = 10, row = 3)),
    River15(imageTileIndex(col = 11, row = 3)),
    Swamp(imageTileIndex(col = 13, row = 12)),
    Crater0(imageTileIndex(col = 0, row = 0)),
    Crater1(imageTileIndex(col = 1, row = 0)),
    Crater2(imageTileIndex(col = 2, row = 0)),
    Crater3(imageTileIndex(col = 3, row = 0)),
    Crater4(imageTileIndex(col = 0, row = 1)),
    Crater5(imageTileIndex(col = 1, row = 1)),
    Crater6(imageTileIndex(col = 2, row = 1)),
    Crater7(imageTileIndex(col = 3, row = 1)),
    Crater8(imageTileIndex(col = 0, row = 2)),
    Crater9(imageTileIndex(col = 1, row = 2)),
    Crater10(imageTileIndex(col = 2, row = 2)),
    Crater11(imageTileIndex(col = 3, row = 2)),
    Crater12(imageTileIndex(col = 0, row = 3)),
    Crater13(imageTileIndex(col = 1, row = 3)),
    Crater14(imageTileIndex(col = 2, row = 3)),
    Crater15(imageTileIndex(col = 3, row = 3)),
    CraterMined0(imageTileIndex(col = 4, row = 0)),
    CraterMined1(imageTileIndex(col = 5, row = 0)),
    CraterMined2(imageTileIndex(col = 6, row = 0)),
    CraterMined3(imageTileIndex(col = 7, row = 0)),
    CraterMined4(imageTileIndex(col = 4, row = 1)),
    CraterMined5(imageTileIndex(col = 5, row = 1)),
    CraterMined6(imageTileIndex(col = 6, row = 1)),
    CraterMined7(imageTileIndex(col = 7, row = 1)),
    CraterMined8(imageTileIndex(col = 4, row = 2)),
    CraterMined9(imageTileIndex(col = 5, row = 2)),
    CraterMined10(imageTileIndex(col = 6, row = 2)),
    CraterMined11(imageTileIndex(col = 7, row = 2)),
    CraterMined12(imageTileIndex(col = 4, row = 3)),
    CraterMined13(imageTileIndex(col = 5, row = 3)),
    CraterMined14(imageTileIndex(col = 6, row = 3)),
    CraterMined15(imageTileIndex(col = 7, row = 3)),
    Road0(imageTileIndex(col = 0, row = 7)),
    Road1(imageTileIndex(col = 1, row = 7)),
    Road2(imageTileIndex(col = 2, row = 7)),
    Road3(imageTileIndex(col = 3, row = 7)),
    Road4(imageTileIndex(col = 4, row = 7)),
    Road5(imageTileIndex(col = 5, row = 7)),
    Road6(imageTileIndex(col = 0, row = 8)),
    Road7(imageTileIndex(col = 1, row = 8)),
    Road8(imageTileIndex(col = 2, row = 8)),
    Road9(imageTileIndex(col = 3, row = 8)),
    Road10(imageTileIndex(col = 4, row = 8)),
    Road11(imageTileIndex(col = 5, row = 8)),
    Road12(imageTileIndex(col = 0, row = 9)),
    Road13(imageTileIndex(col = 1, row = 9)),
    Road14(imageTileIndex(col = 2, row = 9)),
    Road15(imageTileIndex(col = 3, row = 9)),
    Road16(imageTileIndex(col = 4, row = 9)),
    Road17(imageTileIndex(col = 5, row = 9)),
    Road18(imageTileIndex(col = 0, row = 10)),
    Road19(imageTileIndex(col = 1, row = 10)),
    Road20(imageTileIndex(col = 2, row = 10)),
    Road21(imageTileIndex(col = 3, row = 10)),
    Road22(imageTileIndex(col = 4, row = 10)),
    Road23(imageTileIndex(col = 5, row = 10)),
    Road24(imageTileIndex(col = 0, row = 11)),
    Road25(imageTileIndex(col = 1, row = 11)),
    Road26(imageTileIndex(col = 2, row = 11)),
    Road27(imageTileIndex(col = 3, row = 11)),
    Road28(imageTileIndex(col = 4, row = 11)),
    Road29(imageTileIndex(col = 5, row = 11)),
    Road30(imageTileIndex(col = 0, row = 12)),
    RoadMined0(imageTileIndex(col = 6, row = 7)),
    RoadMined1(imageTileIndex(col = 7, row = 7)),
    RoadMined2(imageTileIndex(col = 8, row = 7)),
    RoadMined3(imageTileIndex(col = 9, row = 7)),
    RoadMined4(imageTileIndex(col = 10, row = 7)),
    RoadMined5(imageTileIndex(col = 11, row = 7)),
    RoadMined6(imageTileIndex(col = 6, row = 8)),
    RoadMined7(imageTileIndex(col = 7, row = 8)),
    RoadMined8(imageTileIndex(col = 8, row = 8)),
    RoadMined9(imageTileIndex(col = 9, row = 8)),
    RoadMined10(imageTileIndex(col = 10, row = 8)),
    RoadMined11(imageTileIndex(col = 11, row = 8)),
    RoadMined12(imageTileIndex(col = 6, row = 9)),
    RoadMined13(imageTileIndex(col = 7, row = 9)),
    RoadMined14(imageTileIndex(col = 8, row = 9)),
    RoadMined15(imageTileIndex(col = 9, row = 9)),
    RoadMined16(imageTileIndex(col = 10, row = 9)),
    RoadMined17(imageTileIndex(col = 11, row = 9)),
    RoadMined18(imageTileIndex(col = 6, row = 10)),
    RoadMined19(imageTileIndex(col = 7, row = 10)),
    RoadMined20(imageTileIndex(col = 8, row = 10)),
    RoadMined21(imageTileIndex(col = 9, row = 10)),
    RoadMined22(imageTileIndex(col = 10, row = 10)),
    RoadMined23(imageTileIndex(col = 11, row = 10)),
    RoadMined24(imageTileIndex(col = 6, row = 11)),
    RoadMined25(imageTileIndex(col = 7, row = 11)),
    RoadMined26(imageTileIndex(col = 8, row = 11)),
    RoadMined27(imageTileIndex(col = 9, row = 11)),
    RoadMined28(imageTileIndex(col = 10, row = 11)),
    RoadMined29(imageTileIndex(col = 11, row = 11)),
    RoadMined30(imageTileIndex(col = 6, row = 12)),
    Tree0(imageTileIndex(col = 0, row = 4)),
    Tree1(imageTileIndex(col = 1, row = 4)),
    Tree2(imageTileIndex(col = 2, row = 4)),
    Tree3(imageTileIndex(col = 0, row = 5)),
    Tree4(imageTileIndex(col = 1, row = 5)),
    Tree5(imageTileIndex(col = 2, row = 5)),
    Tree6(imageTileIndex(col = 0, row = 6)),
    Tree7(imageTileIndex(col = 1, row = 6)),
    Tree8(imageTileIndex(col = 2, row = 6)),
    Tree9(imageTileIndex(col = 3, row = 6)),
    TreeMined0(imageTileIndex(col = 4, row = 4)),
    TreeMined1(imageTileIndex(col = 5, row = 4)),
    TreeMined2(imageTileIndex(col = 6, row = 4)),
    TreeMined3(imageTileIndex(col = 4, row = 5)),
    TreeMined4(imageTileIndex(col = 5, row = 5)),
    TreeMined5(imageTileIndex(col = 6, row = 5)),
    TreeMined6(imageTileIndex(col = 4, row = 6)),
    TreeMined7(imageTileIndex(col = 5, row = 6)),
    TreeMined8(imageTileIndex(col = 6, row = 6)),
    TreeMined9(imageTileIndex(col = 7, row = 6)),
    Rubble(imageTileIndex(col = 14, row = 12)),
    Grass(imageTileIndex(col = 12, row = 12)),
    DamagedWall(imageTileIndex(col = 15, row = 11)),
    SwampMined(imageTileIndex(col = 13, row = 13)),
    RubbleMined(imageTileIndex(col = 14, row = 13)),
    GrassMined(imageTileIndex(col = 12, row = 13)),
    BaseNeutral(imageTileIndex(col = 0, row = 13)),
    BaseFriendly(imageTileIndex(col = 1, row = 13)),
    BaseHostile(imageTileIndex(col = 2, row = 13)),
    PillHostile0(imageTileIndex(col = 0, row = 14)),
    PillHostile1(imageTileIndex(col = 1, row = 14)),
    PillHostile2(imageTileIndex(col = 2, row = 14)),
    PillHostile3(imageTileIndex(col = 3, row = 14)),
    PillHostile4(imageTileIndex(col = 4, row = 14)),
    PillHostile5(imageTileIndex(col = 5, row = 14)),
    PillHostile6(imageTileIndex(col = 6, row = 14)),
    PillHostile7(imageTileIndex(col = 7, row = 14)),
    PillHostile8(imageTileIndex(col = 8, row = 14)),
    PillHostile9(imageTileIndex(col = 9, row = 14)),
    PillHostile10(imageTileIndex(col = 10, row = 14)),
    PillHostile11(imageTileIndex(col = 11, row = 14)),
    PillHostile12(imageTileIndex(col = 12, row = 14)),
    PillHostile13(imageTileIndex(col = 13, row = 14)),
    PillHostile14(imageTileIndex(col = 14, row = 14)),
    PillHostile15(imageTileIndex(col = 15, row = 14)),
    PillFriendly0(imageTileIndex(col = 0, row = 15)),
    PillFriendly1(imageTileIndex(col = 1, row = 15)),
    PillFriendly2(imageTileIndex(col = 2, row = 15)),
    PillFriendly3(imageTileIndex(col = 3, row = 15)),
    PillFriendly4(imageTileIndex(col = 4, row = 15)),
    PillFriendly5(imageTileIndex(col = 5, row = 15)),
    PillFriendly6(imageTileIndex(col = 6, row = 15)),
    PillFriendly7(imageTileIndex(col = 7, row = 15)),
    PillFriendly8(imageTileIndex(col = 8, row = 15)),
    PillFriendly9(imageTileIndex(col = 9, row = 15)),
    PillFriendly10(imageTileIndex(col = 10, row = 15)),
    PillFriendly11(imageTileIndex(col = 11, row = 15)),
    PillFriendly12(imageTileIndex(col = 12, row = 15)),
    PillFriendly13(imageTileIndex(col = 13, row = 15)),
    PillFriendly14(imageTileIndex(col = 14, row = 15)),
    PillFriendly15(imageTileIndex(col = 15, row = 15)),
}
