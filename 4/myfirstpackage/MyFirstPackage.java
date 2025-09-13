package myfirstpackage;

public class MyFirstPackage {
    private int a;
    private int b;

    public MyFirstPackage(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public void setA(int a) { this.a = a; }
    public void setB(int b) { this.b = b; }

    public int getA() { return a; }
    public int getB() { return b; }

    public int func() {
        if (b == 0) { // Защита от деления на ноль
            return 0;
        }
        return a % b;
    }
}
