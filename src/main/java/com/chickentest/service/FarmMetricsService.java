package com.chickentest.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class FarmMetricsService {

    private final MeterRegistry meterRegistry;

    public FarmMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementBuy() {
        meterRegistry.counter("farm.operations.buy").increment();
    }

    public void incrementSale() {
        meterRegistry.counter("farm.operations.sale").increment();
    }

    public void incrementAddArticle() {
        meterRegistry.counter("farm.operations.add_article").increment();
    }
    // Podés agregar tags para segmentar por categoría, usuario, etc:
    public void increment(String operation, String category) {
        meterRegistry.counter("farm.operations", "type", operation, "category", category).increment();
    }
    
    // Incrementa en 1 por evento
    public void countHatchedEggs(int units) {
        meterRegistry.counter("farm.hatch.eggs.total").increment(units);
    }

    // O versión sin cantidad (por evento de hatch)
    public void countHatchEvent() {
        meterRegistry.counter("farm.hatch.event.total").increment();
    }   
}