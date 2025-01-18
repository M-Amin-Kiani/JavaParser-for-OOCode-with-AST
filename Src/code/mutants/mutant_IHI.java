package org.example;

public class code1 {

    public static class Vehicle {

        protected String brand;

        private int speed;

        private int fuelCapacity;

        public static int vehicleCount = 0;

        public Vehicle(String brand, int speed, int fuelCapacity) {
            this.brand = brand;
            this.speed = speed;
            this.fuelCapacity = fuelCapacity;
            vehicleCount++;
        }

        public int calculateRange() {
            return speed * fuelCapacity;
        }

        public String displayInfo() {
            return "Brand: " + brand + "\n" + "Speed: " + speed + " km/h\n" + "Fuel Capacity: " + fuelCapacity + " liters";
        }

        public String move() {
            return "The vehicle is moving...";
        }

        public String displayVehicleCount() {
            return "Total Vehicles: " + vehicleCount;
        }
    }

    public static class Car extends Vehicle {

        public static int vehicleCount = 0;

        private int fuelCapacity;

        private int speed;

        protected String brand;

        private int numberOfDoors;

        private int trunkCapacity;

        public Car(String brand, int speed, int fuelCapacity, int numberOfDoors, int trunkCapacity) {
            super(brand, speed, fuelCapacity);
            this.numberOfDoors = numberOfDoors;
            this.trunkCapacity = trunkCapacity;
        }

        @Override
        public String move() {
            return "The car is driving...";
        }

        public int calculateTotalStorage() {
            return trunkCapacity + (numberOfDoors * 10);
        }

        public String displayInfo(boolean showDetails) {
            String info = super.displayInfo();
            if (showDetails) {
                info += "\nNumber of Doors: " + numberOfDoors + "\nTrunk Capacity: " + trunkCapacity + " liters";
            }
            return info;
        }
    }

    public static class Bike extends Vehicle {

        public static int vehicleCount = 0;

        private int fuelCapacity;

        private int speed;

        protected String brand;

        private boolean hasBasket;

        private int basketCapacity;

        public Bike(String brand, int speed, int fuelCapacity, boolean hasBasket, int basketCapacity) {
            super(brand, speed, fuelCapacity);
            this.hasBasket = hasBasket;
            this.basketCapacity = basketCapacity;
        }

        @Override
        public String move() {
            return "The bike is pedaling...";
        }

        public int calculateStorage() {
            return hasBasket ? basketCapacity : 0;
        }

        public int calculateStorage(int additionalItems) {
            int totalCapacity = calculateStorage();
            return totalCapacity + additionalItems * 2;
        }

        @Override
        public String displayInfo() {
            String info = "";
            info = super.displayInfo();
            info += "\nHas Basket: " + (hasBasket ? "Yes" : "No");
            if (hasBasket) {
                info += "\nBasket Capacity: " + basketCapacity + " liters";
            }
            return info;
        }
    }
}
