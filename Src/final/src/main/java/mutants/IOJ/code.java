package mutants.IOJ;

public class code {

    public static class Vehicle {

        protected String brand;

        private int speed;

        public int fuelCapacity;

        public static int vehicleCount = 0;

        public Vehicle() {
            this.brand = "brand";
            this.speed = 0;
            this.fuelCapacity = 0;
            vehicleCount++;
        }

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

        private int numberOfDoors;

        private int trunkCapacity;

        private int fuelCapacity = 9;

        public Car(String brand, int speed, int fuelCapacity, int numberOfDoors, int trunkCapacity) {
            super(brand, speed, fuelCapacity);
            this.numberOfDoors = numberOfDoors;
            this.trunkCapacity = trunkCapacity;
        }

        @Override
        public String move() {
            return "The car is driving...";
        }

        public int getFuelCapacity() {
            return fuelCapacity;
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

        public int getFuelCapacity() {
            return fuelCapacity;
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

    static class UniCycle extends Bike {

        public UniCycle(String brand, int speed, int fuelCapacity, boolean hasBasket, int basketCapacity) {
            super(brand, speed, fuelCapacity, hasBasket, basketCapacity);
        }
    }

    public class Main {

        public static void main(String[] args) {
            Vehicle v = new Car("ghl", 2, 2, 5, 6);
            String y = v.displayVehicleCount();
            Vehicle v1 = new Bike("ll", 20, 20, true, 20);
            Bike b = new UniCycle("llllll", 20, 20, true, 20);
            Vehicle v4 = (Vehicle) v;
            Vehicle v2 = new Vehicle("fffff", 20, 20);
            String x = v2.displayVehicleCount();
            Vehicle v22 = (Bike) v2;
            Vehicle vehicle = (Vehicle) new Vehicle("sd", 12, 25);
            Car myCar = new Car("ll", 120, 24, 5, 6);
            Vehicle vehicle1;
            vehicle1 = myCar;
        }
    }

    public static class Test {

        public void main() {
            Car myCar = new Car("ll", 120, 24, 5, 6);
        }
    }

    public static boolean isNull(Vehicle v) {
        return v == null;
    }

    public static boolean equals(Bike b) {
        return b == null;
    }
}
