package com.revline.ui;

/**
 * Console presentation banners and ASCII art preserving the C prototype look.
 */
public final class Banner {

    private Banner() {}

    public static void printLogo() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                    ║");
        System.out.println("║         ____                       _       _                       ║");
        System.out.println("║        |  _ \\                     | |     (_)                      ║");
        System.out.println("║        | |_) |  ___   __   __    | |      _   _ __    ___         ║");
        System.out.println("║        |  _ <  / _ \\  \\ \\ / /    | |     | | | '_ \\  / _ \\         ║");
        System.out.println("║        | | \\ \\ |  __/  \\ V /     | |___  | | | | | ||  __/         ║");
        System.out.println("║        |_|  \\_\\ \\___|    \\_/      |_____| |_| |_| |_|\\___|         ║");
        System.out.println("║                                                                    ║");
        System.out.println("║                    Drive Passion. Live RevLine.                    ║");
        System.out.println("║                                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    public static void printBootPanel(String storageMode, String filePath, boolean isNew, int recordsLoaded) {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SYSTEM BOOT: STORAGE & PERSISTENCE INITIALIZATION                  ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ [INFO] Storage Engine : %-42s ║\n", storageMode);
        System.out.printf("║ [INFO] Database File  : %-42s ║\n", filePath);
        if (isNew) {
            System.out.println("║ [WARN] Database empty or missing. Loading seed supercars...        ║");
            System.out.println("║ [OK]   18 premium vehicles successfully seeded and saved to disk.  ║");
        } else {
            System.out.printf("║ [INFO] Scanning verified records... %-30d ║\n", recordsLoaded);
            System.out.println("║ [OK]   Database verified and securely loaded into memory.          ║");
        }
        System.out.println("║ [OK]   Audit logger active at logs/audit.log                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }
}
