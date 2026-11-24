package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Sale;

/**
 * Formats official showroom sales receipts in clean ASCII presentation.
 */
final class SalesReceiptFormatter {

    private SalesReceiptFormatter() {}

    static String format(Sale sale, Car car) {
        if (sale == null) return "";
        String brandModel = (car != null) ? (car.getBrand() + " " + car.getModel()) : ("Car #" + sale.getCarId());
        String engineInfo = (car != null) ? (car.getHorsepower() + " HP | " + car.getTopSpeed() + " km/h") : "N/A";
        String fuelInfo = (car != null) ? car.getFuelType().name() : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("=======================================================").append(System.lineSeparator());
        sb.append("                   REVLINE SHOWROOM                    ").append(System.lineSeparator());
        sb.append("                OFFICIAL SALES RECEIPT                 ").append(System.lineSeparator());
        sb.append("=======================================================").append(System.lineSeparator());
        sb.append(String.format("  Receipt No   : REC-%04d-%04d%n", sale.getSaleDate().getYear(), sale.getSaleId()));
        sb.append(String.format("  Sale ID      : #%d%n", sale.getSaleId()));
        sb.append(String.format("  Date         : %s%n", sale.getSaleDate()));
        sb.append(String.format("  Buyer        : %s%n", sale.getBuyerName()));
        sb.append(String.format("  Seller       : %s%n", sale.getSellerName()));
        sb.append("-------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("  Vehicle ID   : #%d%n", sale.getCarId()));
        sb.append(String.format("  Vehicle      : %s%n", brandModel));
        sb.append(String.format("  Specs        : %s%n", engineInfo));
        sb.append(String.format("  Fuel Type    : %s%n", fuelInfo));
        sb.append("-------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("  Selling Price: $%,.2f%n", sale.getPrice()));
        sb.append("  Taxes & Fees : INCLUDED").append(System.lineSeparator());
        sb.append(String.format("  TOTAL PAID   : $%,.2f%n", sale.getPrice()));
        sb.append("=======================================================").append(System.lineSeparator());
        sb.append("            THANK YOU FOR CHOOSING REVLINE!            ").append(System.lineSeparator());
        sb.append("=======================================================").append(System.lineSeparator());
        return sb.toString();
    }
}
