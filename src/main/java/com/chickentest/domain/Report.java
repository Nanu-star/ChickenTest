package com.chickentest.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    private String date;

    // Inventario actual
    private int totalEggs;
    private int totalChickens;

    // Producción y movimientos
    private int producedBatches;
    private int bornChickens;
    private int deceasedChickens;

    // Operaciones
    private int purchasedBatches;
    private int soldBatches;
    private int purchasedChickens;
    private int soldChickens;

    // Finanzas
    private double totalPurchases;
    private double totalSales;

    // Derivados útiles
    public double getBalance() {
        return totalSales - totalPurchases;
    }
}