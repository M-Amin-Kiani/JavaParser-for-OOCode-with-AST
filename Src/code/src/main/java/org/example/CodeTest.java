package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CodeTest {

    private static code.Vehicle vehicle;
    private static code.Car car;
    private static code.Bike bike;

    @BeforeAll
    public static void setUp() {
        vehicle = new code.Vehicle("Generic", 80, 50);
        car = new code.Car("Toyota", 120, 40, 4, 500);
        bike = new code.Bike("Schwinn", 15, 0, true, 20);
    }

    @Test
    public void testVehicleCalculateRange() {
        int range = vehicle.calculateRange();
        Assertions.assertEquals(4000, range, "Vehicle range calculation is incorrect");
    }

    @Test
    public void testVehicleDisplayInfo() {
        String info = vehicle.displayInfo();
        Assertions.assertTrue(info.contains("Brand: Generic"), "Vehicle info should contain brand");
        Assertions.assertTrue(info.contains("Speed: 80 km/h"), "Vehicle info should contain speed");
        Assertions.assertTrue(info.contains("Fuel Capacity: 50 liters"), "Vehicle info should contain fuel capacity");
    }

    @Test
    public void testVehicleMove() {
        String moveMessage = vehicle.move();
        Assertions.assertEquals("The vehicle is moving...", moveMessage, "Vehicle move message is incorrect");
    }

    @Test
    public void testVehicleCount() {
        String countMessage = vehicle.displayVehicleCount();
        Assertions.assertEquals("Total Vehicles: 3", countMessage, "Vehicle count is incorrect");
    }

    @Test
    public void testCarCalculateRange() {
        int range = car.calculateRange();
        Assertions.assertEquals(4800, range, "Car range calculation is incorrect");
    }

    @Test
    public void testCarDisplayInfoWithoutDetails() {
        String info = car.displayInfo(false);
        Assertions.assertTrue(info.contains("Brand: Toyota"), "Car info should contain brand");
        assertFalse(info.contains("Number of Doors:"), "Car info should not contain details");
    }

    @Test
    public void testCarDisplayInfoWithDetails() {
        String info = car.displayInfo(true);
        Assertions.assertTrue(info.contains("Number of Doors: 4"), "Car info should contain number of doors");
        Assertions.assertTrue(info.contains("Trunk Capacity: 500 liters"), "Car info should contain trunk capacity");
    }

    @Test
    public void testCarMove() {
        String moveMessage = car.move();
        Assertions.assertEquals("The car is driving...", moveMessage, "Car move message is incorrect");
    }

    @Test
    public void testCarCalculateTotalStorage() {
        int totalStorage = car.calculateTotalStorage();
        Assertions.assertEquals(540, totalStorage, "Car total storage calculation is incorrect");
    }

    @Test
    public void testCarGetFuelCapacity() {
        int fuelCapacity = car.getFuelCapacity();
        Assertions.assertEquals(9, fuelCapacity, "Car fuel capacity getter is incorrect");
    }

    @Test
    public void testBikeGetFuelCapacity() {
        int fuelCapacity = bike.getFuelCapacity();
        Assertions.assertEquals(0, fuelCapacity, "bike fuel capacity getter is incorrect");
    }

    @Test
    public void testBikeCalculateRange() {
        int range = bike.calculateRange();
        Assertions.assertEquals(0, range, "Bike range calculation is incorrect");
    }

    @Test
    public void testBikeDisplayInfo() {
        String info = bike.displayInfo();
        Assertions.assertTrue(info.contains("Brand: Schwinn"), "Bike info should contain brand");
        Assertions.assertTrue(info.contains("Has Basket: Yes"), "Bike info should contain basket info");
        Assertions.assertTrue(info.contains("Basket Capacity: 20 liters"), "Bike info should contain basket capacity");
    }

    @Test
    public void testBikeMove() {
        String moveMessage = bike.move();
        Assertions.assertEquals("The bike is pedaling...", moveMessage, "Bike move message is incorrect");
    }

    @Test
    public void testBikeCalculateStorageWithoutAdditionalItems() {
        int storage = bike.calculateStorage();
        Assertions.assertEquals(20, storage, "Bike storage calculation without additional items is incorrect");
    }

    @Test
    public void testBikeCalculateStorageWithAdditionalItems() {
        int storage = bike.calculateStorage(5);
        Assertions.assertEquals(30, storage, "Bike storage calculation with additional items is incorrect");
    }
}

