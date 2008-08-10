package jatran.stub;

public class VariableParameterDef {
    public static void main(String[] args) {
        System.out.println(sum(3, 44));
        System.out.println(sum(4, 4, 5, 45, 45));
    }
    
    public static int sum(int ... values) {
        int res;
        for (int i : values) {
            res += i;
        }
        return res;
    } 
}

