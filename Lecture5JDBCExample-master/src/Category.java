public class Category {
    private String name;
    private double multiplier;

    public Category(String name, double multiplier) {
        this.name = name;
        this.multiplier = multiplier;
    }

    public String getName() { return name; }
    public double getMultiplier() { return multiplier; }
}