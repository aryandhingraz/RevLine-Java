#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_CARS 100
#define FILENAME "cars.txt"

// ==========================================
// CORE DATA STRUCTURE
// ==========================================
typedef struct {
    int id;
    char brand[50];
    char model[50];
    double price;
    int topSpeed;
    char fuelType[20];
    int horsepower;
    double acceleration; // 0-100 km/h time
    int isAvailable;     // 1 for Available, 0 for Sold
} Car;

// ==========================================
// FUNCTION PROTOTYPES (For future steps)
// ==========================================
void displayMenu();
int loadCarsFromFile(Car *showroom);
void displayAllCars(Car *showroom, int count);
void addCar(Car *showroom, int *count);
void searchCar(Car *showroom, int count);
void filterCars(Car *showroom, int count);
void sortCars(Car *showroom, int count);
void buyCar(Car *showroom, int count);
void saveCarsToFile(Car *showroom, int count);
void loadDefaultCars(Car *showroom, int *count);
void initDatabase(Car *showroom, int *carCount);
void printTableTop(const char* title);
void printTableRow(Car car);
void printTableBottom(int totalCount, int availableCount, double totalValue);

void initDatabase(Car *showroom, int *carCount) {
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ SYSTEM BOOT: FILE HANDLING & DATABASE SETUP                        ║\n");
    printf("╠════════════════════════════════════════════════════════════════════╣\n");
    printf("║ [INFO] Checking database file '%-35s ║\n", "cars.txt'...");

    FILE *file = fopen(FILENAME, "r");
    if (file == NULL) {
        printf("║ [WARN] File not found or empty. Creating new file... [OK]          ║\n");
        file = fopen(FILENAME, "w");
        if (file) fclose(file);
        *carCount = 0;
    } else {
        fclose(file);
        *carCount = loadCarsFromFile(showroom);
    }

    if (*carCount == 0) {
        printf("║ [WARN] Database is empty. Loading factory defaults...              ║\n");
        loadDefaultCars(showroom, carCount);
        saveCarsToFile(showroom, *carCount);
        printf("║ [OK]   18 premium vehicles successfully loaded into memory.        ║\n");
        printf("║ [OK]   Default database securely saved to disk.                    ║\n");
    } else {
        printf("║ [INFO] Scanning records... %-39d ║\n", *carCount);
        printf("║ [OK]   Database verified and securely loaded into memory.          ║\n");
    }
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
}

// ==========================================
// INITIAL SETUP & ENTRY POINT
// ==========================================
int main() {
    // Dynamic memory allocation for scalable storage
    Car *showroom = (Car *)malloc(MAX_CARS * sizeof(Car));
    int carCount = 0;

    if (showroom == NULL) {
        printf("Error: Memory allocation failed!\n");
        return 1;
    }

    system("chcp 65001 > nul"); // Ensure UTF-8 output on Windows for box characters

    // Step 1: Rigorous file verification & default injection
    initDatabase(showroom, &carCount);
    
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║                                                                    ║\n");
    printf("║         ____                       _       _                       ║\n");
    printf("║        |  _ \\                     | |     (_)                      ║\n");
    printf("║        | |_) |  ___   __   __    | |      _   _ __    ___         ║\n");
    printf("║        |  _ <  / _ \\  \\ \\ / /    | |     | | | '_ \\  / _ \\         ║\n");
    printf("║        | | \\ \\ |  __/  \\ V /     | |___  | | | | | ||  __/         ║\n");
    printf("║        |_|  \\_\\ \\___|    \\_/      |_____| |_| |_| |_|\\___|         ║\n");
    printf("║                                                                    ║\n");
    printf("║                    Drive Passion. Live RevLine.                    ║\n");
    printf("║                                                                    ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");

    // Step 3: Main interactive loop
    int choice = -1;
    while (choice != 0) {
        displayMenu();
        if (scanf("%d", &choice) != 1) {
            printf("| Error: Invalid input. Please enter a number.\n");
            while (getchar() != '\n'); // clear input buffer
            continue;
        }

        switch (choice) {
            case 1:
                addCar(showroom, &carCount);
                saveCarsToFile(showroom, carCount);
                break;
            case 2:
                displayAllCars(showroom, carCount);
                break;
            case 3:
                searchCar(showroom, carCount);
                break;
            case 4:
                filterCars(showroom, carCount);
                break;
            case 5:
                sortCars(showroom, carCount);
                saveCarsToFile(showroom, carCount);
                break;
            case 6:
                buyCar(showroom, carCount);
                saveCarsToFile(showroom, carCount);
                break;
            case 0:
                printf("\nExiting RevLine System... Goodbye!\n");
                break;
            default:
                printf("\n| Error: Invalid option. Try again.\n");
        }
    }

    // Free memory before exiting
    free(showroom);
    return 0;
}

// UI Main Menu Function
void displayMenu() {
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ MENU: SYSTEM OPERATIONS                                            ║\n");
    printf("╠════════════════════════════════════════════════════════════════════╣\n");
    printf("║                                                                    ║\n");
    printf("║  [1] Add New Car                                                   ║\n");
    printf("║  [2] Display All Cars                                              ║\n");
    printf("║  [3] Search Car                                                    ║\n");
    printf("║  [4] Filter Cars                                                   ║\n");
    printf("║  [5] Sort Cars                                                     ║\n");
    printf("║  [6] Buy a Car                                                     ║\n");
    printf("║                                                                    ║\n");
    printf("║  [0] Exit Application                                              ║\n");
    printf("║                                                                    ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
    printf("   Select an option: ");
}

// ==========================================
// FILE HANDLING FUNCTIONS
// ==========================================
int loadCarsFromFile(Car *showroom) {
    FILE *file = fopen(FILENAME, "r");
    if (file == NULL) {
        // File doesn't exist yet, return 0 cars loaded
        return 0;
    }

    int count = 0;
    // Read format: id,brand,model,price,topSpeed,fuelType,horsepower,acceleration,isAvailable
    // Used %49[^,] etc to prevent string buffer overflow during reading
    while (fscanf(file, "%d,%49[^,],%49[^,],%lf,%d,%19[^,],%d,%lf,%d\n",
                  &showroom[count].id,
                  showroom[count].brand,
                  showroom[count].model,
                  &showroom[count].price,
                  &showroom[count].topSpeed,
                  showroom[count].fuelType,
                  &showroom[count].horsepower,
                  &showroom[count].acceleration,
                  &showroom[count].isAvailable) == 9) {
        count++;
        if (count >= MAX_CARS) break; // Prevent buffer overflow
    }

    fclose(file);
    return count;
}

void saveCarsToFile(Car *showroom, int count) {
    FILE *file = fopen(FILENAME, "w");
    if (file == NULL) {
        printf("\n╔════════════════════════════════════════════════════════════════════╗\n");
        printf("║ [ERROR] Critical failure: Unable to open file for saving!          ║\n");
        printf("╚════════════════════════════════════════════════════════════════════╝\n");
        return;
    }

    // Write all records perfectly matching the load function format
    for (int i = 0; i < count; i++) {
        fprintf(file, "%d,%s,%s,%lf,%d,%s,%d,%lf,%d\n",
                showroom[i].id,
                showroom[i].brand,
                showroom[i].model,
                showroom[i].price,
                showroom[i].topSpeed,
                showroom[i].fuelType,
                showroom[i].horsepower,
                showroom[i].acceleration,
                showroom[i].isAvailable);
    }

    fclose(file);
}

// ==========================================
// DISPLAY & TABLE HELPERS
// ==========================================
void printTableTop(const char* title) {
    printf("\n");
    printf("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗\n");
    printf("║ %-100s ║\n", title);
    printf("╠════╦═════════════════╦═══════════════════╦═══════════╦═══════╦══════╦═══════╦════════════╦═══════════╣\n");
    printf("║ ID ║ Brand           ║ Model             ║ Price ($) ║ Speed ║ HP   ║ Accel ║ Fuel Type  ║ Status    ║\n");
    printf("╠════╬═════════════════╬═══════════════════╬═══════════╬═══════╬══════╬═══════╬════════════╬═══════════╣\n");
}

void printTableRow(Car car) {
    printf("║ %-2d ║ %-15s ║ %-17s ║ %-9.0f ║ %-5d ║ %-4d ║ %-5.1f ║ %-10s ║ %-9s ║\n",
           car.id, car.brand, car.model, car.price, car.topSpeed, car.horsepower, car.acceleration, car.fuelType, car.isAvailable ? "Available" : "Sold");
}

void printTableBottom(int totalCount, int availableCount, double totalValue) {
    printf("╚════╩═════════════════╩═══════════════════╩═══════════╩═══════╩══════╩═══════╩════════════╩═══════════╝\n");
    if (totalCount >= 0) {
        char footer[150];
        snprintf(footer, sizeof(footer), "FOOTER: Total Cars: %d  |  Available: %d  |  Total Showroom Value: $%.2f", totalCount, availableCount, totalValue);
        printf("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗\n");
        printf("║ %-100s ║\n", footer);
        printf("╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝\n");
    }
}

// ==========================================
// DISPLAY FUNCTIONS
// ==========================================
void displayAllCars(Car *showroom, int count) {
    if (count == 0) {
        printf("\n");
        printf("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗\n");
        printf("║ %-100s ║\n", "No cars currently available in the showroom.");
        printf("╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝\n");
        return;
    }

    printTableTop("DATA: SHOWROOM INVENTORY");

    double totalValue = 0;
    int availableCount = 0;

    for (int i = 0; i < count; i++) {
        printTableRow(showroom[i]);
               
        totalValue += showroom[i].price;
        if (showroom[i].isAvailable == 1) {
            availableCount++;
        }
    }
    printTableBottom(count, availableCount, totalValue);
}

// ==========================================
// DATA MODIFICATION FUNCTIONS
// ==========================================
void addCar(Car *showroom, int *count) {
    if (*count >= MAX_CARS) {
        printf("\n| Error: Showroom is at maximum capacity (%d cars)!\n", MAX_CARS);
        return;
    }

    Car newCar;
    
    // Auto-generate ID based on the highest existing ID to maintain uniqueness
    int maxId = 0;
    for (int i = 0; i < *count; i++) {
        if (showroom[i].id > maxId) {
            maxId = showroom[i].id;
        }
    }
    newCar.id = maxId + 1;

    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ ADD A NEW CAR                                                      ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");

    printf("  Brand: ");
    scanf(" %49[^\n]", newCar.brand);
    
    printf("  Model: ");
    scanf(" %49[^\n]", newCar.model);
    
    printf("  Price ($): ");
    scanf("%lf", &newCar.price);
    
    printf("  Top Speed (km/h): ");
    scanf("%d", &newCar.topSpeed);
    
    printf("  Fuel Type: ");
    scanf(" %19[^\n]", newCar.fuelType);
    
    printf("  Horsepower (HP): ");
    scanf("%d", &newCar.horsepower);
    
    printf("  Acceleration (0-100 km/h in seconds): ");
    scanf("%lf", &newCar.acceleration);
    
    int statusStatus;
    printf("  Status (1 for Available, 0 for Sold): ");
    while (scanf("%d", &statusStatus) != 1 || (statusStatus != 0 && statusStatus != 1)) {
        printf("  Invalid input. Enter 1 (Available) or 0 (Sold): ");
        while (getchar() != '\n'); // flush invalid input
    }
    newCar.isAvailable = statusStatus;

    // Add into array
    showroom[*count] = newCar;
    (*count)++;

    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    char msg1[100];
    char msg2[100];
    snprintf(msg1, sizeof(msg1), "SUCCESS: '%s %s' has been successfully added!", newCar.brand, newCar.model);
    snprintf(msg2, sizeof(msg2), "Assigned Car ID: %d", newCar.id);
    printf("║ %-66s ║\n", msg1);
    printf("║ %-66s ║\n", msg2);
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
}

// ==========================================
// SEARCH FUNCTIONS
// ==========================================
void searchCar(Car *showroom, int count) {
    if (count == 0) {
        printf("\n| Search unavailable: Showroom is empty.\n");
        return;
    }

    int choice;
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ MENU: SEARCH CAR                                                   ║\n");
    printf("╠════════════════════════════════════════════════════════════════════╣\n");
    printf("║                                                                    ║\n");
    printf("║  [1] Search by ID                                                  ║\n");
    printf("║  [2] Search by Brand                                               ║\n");
    printf("║                                                                    ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
    printf("   Select search method: ");
    
    if (scanf("%d", &choice) != 1) {
        printf("  Error: Invalid input.\n");
        while(getchar() != '\n'); 
        return;
    }

    int foundCount = 0;
    if (choice == 1) {
        int searchId;
        printf("  Enter Car ID: ");
        if (scanf("%d", &searchId) == 1) {
            printTableTop("DATA: SEARCH RESULTS");
            for (int i = 0; i < count; i++) {
                if (showroom[i].id == searchId) {
                    printTableRow(showroom[i]);
                    foundCount++;
                    break;
                }
            }
        }
    } else if (choice == 2) {
        char searchBrand[50];
        printf("  Enter Brand Name (Case-sensitive): ");
        scanf(" %[^\n]", searchBrand);
        
        printTableTop("DATA: SEARCH RESULTS");
        for (int i = 0; i < count; i++) {
            // Using strstr for substring matching (e.g., 'Fer' finds 'Ferrari')
            if (strstr(showroom[i].brand, searchBrand) != NULL) {
                printTableRow(showroom[i]);
                foundCount++;
            }
        }
    } else {
        printf("  Error: Invalid choice.\n");
        return;
    }

    if (foundCount == 0) {
        printf("║ %-100s ║\n", "No cars found matching your search criteria.");
    }
    printTableBottom(foundCount, foundCount, 0);
}

// ==========================================
// FILTER FUNCTIONS
// ==========================================
void filterCars(Car *showroom, int count) {
    if (count == 0) {
        printf("\n| Filter unavailable: Showroom is empty.\n");
        return;
    }

    int choice;
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ MENU: FILTER CARS                                                  ║\n");
    printf("╠════════════════════════════════════════════════════════════════════╣\n");
    printf("║                                                                    ║\n");
    printf("║  [1] Show Available Cars Only                                      ║\n");
    printf("║  [2] Filter by Maximum Price                                       ║\n");
    printf("║                                                                    ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
    printf("   Select filter method: ");
    
    if (scanf("%d", &choice) != 1) {
        printf("  Error: Invalid input.\n");
        while(getchar() != '\n'); 
        return;
    }

    int foundCount = 0;
    double maxPrice = 0.0;

    if (choice == 2) {
        printf("  Enter Maximum Budget ($): ");
        if (scanf("%lf", &maxPrice) != 1) {
            printf("  Error: Invalid input.\n");
            while(getchar() != '\n');
            return;
        }
    }

    if (choice != 1 && choice != 2) {
        printf("  Error: Invalid choice.\n");
        return;
    }

    printTableTop("DATA: FILTER RESULTS");

    for (int i = 0; i < count; i++) {
        int match = 0;
        if (choice == 1 && showroom[i].isAvailable == 1) {
            match = 1;
        } else if (choice == 2 && showroom[i].price <= maxPrice) {
            match = 1;
        }

        if (match) {
            printTableRow(showroom[i]);
            foundCount++;
        }
    }

    if (foundCount == 0) {
        printf("║ %-100s ║\n", "No cars found matching your filter criteria.");
    }
    printf("╚════╩═════════════════╩═══════════════════╩═══════════╩═══════╩══════╩═══════╩════════════╩═══════════╝\n");
    char res[100];
    snprintf(res, sizeof(res), "Total Filtered: %d", foundCount);
    printf("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗\n");
    printf("║ %-100s ║\n", res);
    printf("╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝\n");
}

// ==========================================
// SORTING FUNCTIONS
// ==========================================
void sortCars(Car *showroom, int count) {
    if (count < 2) {
        printf("\n| Error: Not enough cars to sort.\n");
        return;
    }

    int choice;
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ MENU: SORT CARS                                                    ║\n");
    printf("╠════════════════════════════════════════════════════════════════════╣\n");
    printf("║                                                                    ║\n");
    printf("║  [1] Sort by Price (Low to High)                                   ║\n");
    printf("║  [2] Sort by Price (High to Low)                                   ║\n");
    printf("║  [3] Sort by Top Speed (Highest First)                             ║\n");
    printf("║  [4] Sort by Horsepower (Highest First)                            ║\n");
    printf("║                                                                    ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
    printf("   Select sort method: ");
    
    if (scanf("%d", &choice) != 1) {
        printf("  Error: Invalid input.\n");
        while(getchar() != '\n');
        return;
    }

    // Implementing Bubble Sort (Efficient enough for N=100)
    for (int i = 0; i < count - 1; i++) {
        for (int j = 0; j < count - i - 1; j++) {
            int swap = 0;
            if (choice == 1 && showroom[j].price > showroom[j+1].price) swap = 1;
            else if (choice == 2 && showroom[j].price < showroom[j+1].price) swap = 1;
            else if (choice == 3 && showroom[j].topSpeed < showroom[j+1].topSpeed) swap = 1;
            else if (choice == 4 && showroom[j].horsepower < showroom[j+1].horsepower) swap = 1;

            if (swap) {
                Car temp = showroom[j];
                showroom[j] = showroom[j+1];
                showroom[j+1] = temp;
            }
        }
    }

    if (choice >= 1 && choice <= 4) {
        printf("\n  SUCCESS: Cars sorted successfully!\n");
        displayAllCars(showroom, count);
    } else {
        printf("  Error: Invalid choice.\n");
    }
}

// ==========================================
// TRANSACTION FUNCTIONS
// ==========================================
void buyCar(Car *showroom, int count) {
    if (count == 0) {
        printf("\n| Transaction unavailable: Showroom is empty.\n");
        return;
    }

    int searchId;
    printf("\n");
    printf("╔════════════════════════════════════════════════════════════════════╗\n");
    printf("║ MENU: BUY A CAR                                                    ║\n");
    printf("╚════════════════════════════════════════════════════════════════════╝\n");
    printf("   Enter the ID of the car you wish to buy: ");
    
    if (scanf("%d", &searchId) != 1) {
        printf("  Error: Invalid input.\n");
        while(getchar() != '\n');
        return;
    }

    int foundIndex = -1;
    for (int i = 0; i < count; i++) {
        if (showroom[i].id == searchId) {
            foundIndex = i;
            break;
        }
    }

    if (foundIndex == -1) {
        printf("  Error: Car with ID %d not found.\n", searchId);
    } else {
        if (showroom[foundIndex].isAvailable == 0) {
            printf("\n  Apologies, the '%s %s' is already Sold!\n", showroom[foundIndex].brand, showroom[foundIndex].model);
        } else {
            showroom[foundIndex].isAvailable = 0; // Mark as Sold
            printf("\n");
            printf("  ╔════════════════════════════════════════════════════════════════╗\n");
            printf("  ║                  R E V L I N E   M O T O R S                   ║\n");
            printf("  ╠════════════════════════════════════════════════════════════════╣\n");
            printf("  ║ RECEIPT OF PURCHASE                                            ║\n");
            printf("  ║                                                                ║\n");
            printf("  ║ Brand: %-55s ║\n", showroom[foundIndex].brand);
            printf("  ║ Model: %-55s ║\n", showroom[foundIndex].model);
            printf("  ║ Price Paid: $%-50.2f ║\n", showroom[foundIndex].price);       
            printf("  ║                                                                ║\n");
            printf("  ║ CONGRATULATIONS ON YOUR NEW SUPERCAR!                          ║\n");
            printf("  ╚════════════════════════════════════════════════════════════════╝\n");
        }
    }
}

// ==========================================
// PRELOADED DEFAULT DATA (STEP 3)
// ==========================================
void loadDefaultCars(Car *showroom, int *count) {
    Car defaultCars[] = {
        {1, "Bugatti", "Chiron", 3000000.0, 420, "Petrol", 1500, 2.4, 1},
        {2, "Bugatti", "Veyron SS", 2500000.0, 431, "Petrol", 1200, 2.5, 1},
        {3, "Ferrari", "LaFerrari", 1400000.0, 350, "Hybrid", 949, 2.4, 1},
        {4, "Ferrari", "SF90 Stradale", 507000.0, 340, "Hybrid", 986, 2.5, 1},
        {5, "Lamborghini", "Aventador SVJ", 517770.0, 350, "Petrol", 759, 2.8, 1},
        {6, "Lamborghini", "Huracan STO", 327838.0, 310, "Petrol", 631, 3.0, 1},
        {7, "Porsche", "911 Turbo S", 207000.0, 330, "Petrol", 640, 2.6, 1},
        {8, "Porsche", "Carrera GT", 448000.0, 330, "Petrol", 605, 3.5, 1},
        {9, "McLaren", "P1", 1150000.0, 350, "Hybrid", 903, 2.8, 1},
        {10, "McLaren", "720S", 305000.0, 341, "Petrol", 710, 2.9, 1},
        {11, "Aston Martin", "Valkyrie", 3000000.0, 402, "Hybrid", 1140, 2.5, 1},
        {12, "Tesla", "Model S Plaid", 135990.0, 322, "Electric", 1020, 1.99, 1},
        {13, "Koenigsegg", "Jesko", 3000000.0, 480, "Petrol", 1600, 2.5, 1},
        {14, "Koenigsegg", "Agera RS", 2500000.0, 447, "Petrol", 1341, 2.8, 1},
        {15, "Pagani", "Huayra", 1400000.0, 383, "Petrol", 720, 2.8, 1},
        {16, "Pagani", "Zonda Cinque", 2000000.0, 350, "Petrol", 669, 3.4, 1},
        {17, "BMW", "M5 CS", 142000.0, 305, "Petrol", 627, 2.9, 1},
        {18, "Audi", "R8 V10 Plus", 197000.0, 330, "Petrol", 602, 3.2, 1}
    };
    
    int numDefaults = sizeof(defaultCars) / sizeof(defaultCars[0]);
    for (int i = 0; i < numDefaults && i < MAX_CARS; i++) {
        showroom[i] = defaultCars[i];
    }
    *count = numDefaults;
}
