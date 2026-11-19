package com.revline.ui;

/**
 * Menu printing utilities using clean box-drawing frames.
 */
public final class MenuPrinter {

    private MenuPrinter() {}

    public static void printMainMenu() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ MENU: REVLINE OPERATIONS                                           ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                                    ║");
        System.out.println("║  [1] Display All Supercars (Full Inventory)                        ║");
        System.out.println("║  [2] Add New Supercar                                              ║");
        System.out.println("║  [3] Search Supercar (by ID or Multi-Field Text)                   ║");
        System.out.println("║  [4] View Brand Directory (Sorted A-Z)                             ║");
        System.out.println("║  [5] Showroom Statistics & Summary                                 ║");
        System.out.println("║                                                                    ║");
        System.out.println("║  [0] Exit Application                                              ║");
        System.out.println("║                                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    public static void printSearchMenu() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SUB-MENU: SEARCH SUPERCAR                                          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                                    ║");
        System.out.println("║  [1] Search by Exact Car ID                                        ║");
        System.out.println("║  [2] Multi-Field Text Search (Brand, Model, Fuel) [EX-3]          ║");
        System.out.println("║  [0] Return to Main Menu                                           ║");
        System.out.println("║                                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }
}
