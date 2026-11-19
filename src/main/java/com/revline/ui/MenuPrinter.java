package com.revline.ui;

/**
 * Menu printing utilities using clean box-drawing frames.
 */
public final class MenuPrinter {

    private MenuPrinter() {}

    public static void printBuyerView() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ REVLINE SHOWROOM — BUYER / CLIENT VIEW                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                                    ║");
        System.out.println("║  [1] Browse Showroom Supercars (Interactive Next / Previous)       ║");
        System.out.println("║  [2] Search & Filter Supercars (Multi-Field Search & Budget)       ║");
        System.out.println("║  [3] Sort Supercars (Price, Top Speed, HP, EX-2 Value-for-Money)   ║");
        System.out.println("║  [4] Side-by-Side Supercar Comparison (2-4 Cars Spec Matrix)       ║");
        System.out.println("║  [5] Place Temporary Reservation (Hold Vehicle with Time-to-Live)  ║");
        System.out.println("║  [6] Purchase Supercar (Execute Sale & Generate Official Receipt)  ║");
        System.out.println("║  [7] Join Customer Waitlist (FIFO Priority for Reserved Cars)      ║");
        System.out.println("║  [8] Recently Viewed Supercars (LRU Cache)                         ║");
        System.out.println("║                                                                    ║");
        System.out.println("║  [S] Switch to SELLER / MANAGEMENT VIEW                            ║");
        System.out.println("║  [0] Exit Application                                              ║");
        System.out.println("║                                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    public static void printSellerView() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ REVLINE INVENTORY — SELLER / MANAGEMENT VIEW                       ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                                    ║");
        System.out.println("║  [1] View Complete Showroom Inventory                              ║");
        System.out.println("║  [2] Add New Supercar to Inventory                                 ║");
        System.out.println("║  [3] Update Supercar Specifications                                ║");
        System.out.println("║  [4] Delete Supercar from Inventory                                ║");
        System.out.println("║  [5] Undo Last Inventory Action [EX-7]                             ║");
        System.out.println("║  [6] Sales Transaction History & Receipts                          ║");
        System.out.println("║  [7] Showroom Financial Summary & Stock Valuation                  ║");
        System.out.println("║  [8] View System Audit Log (Recent Operations)                     ║");
        System.out.println("║                                                                    ║");
        System.out.println("║  [S] Switch to BUYER / CLIENT VIEW                                 ║");
        System.out.println("║  [0] Exit Application                                              ║");
        System.out.println("║                                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    public static void printSortMenu() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SORT OPTIONS (CarComparators - L8)                                 ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  [1] Price: Low to High                                            ║");
        System.out.println("║  [2] Price: High to Low                                            ║");
        System.out.println("║  [3] Top Speed: Highest First                                      ║");
        System.out.println("║  [4] Horsepower: Most Powerful First                               ║");
        System.out.println("║  [5] Acceleration: 0-100 km/h Quickest First                       ║");
        System.out.println("║  [6] Brand & Model: Alphabetical (A-Z)                             ║");
        System.out.println("║  [7] Value-for-Money: Horsepower per $100k [EX-2]                  ║");
        System.out.println("║  [0] Back to Menu                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    public static void printSearchFilterMenu() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SEARCH & FILTER OPTIONS (FilterBuilder - L21)                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  [1] Search by Exact Car ID                                        ║");
        System.out.println("║  [2] Multi-Field Text Search (Brand, Model, Fuel) [EX-3]          ║");
        System.out.println("║  [3] Budget Filter (Price Range)                                   ║");
        System.out.println("║  [4] Performance Filter (Min Speed & Min Horsepower)               ║");
        System.out.println("║  [5] Available Cars Only                                           ║");
        System.out.println("║  [0] Back to Menu                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }
}
