public class Bar implements Comparable<Bar>{
    
    String catagory;
    String name;
    int value;
    
    public Bar(String name, int value, String category){
        if(name == null || category == null){
            throw new IllegalArgumentException();
        }
        if(value < 0){
            throw new IllegalArgumentException();
        }
        
        this.name = name;
        this.value = value;
        this.catagory = category;
    }

    public String getName() {
        return this.name;
    }

    public int getValue(){
        return this.value;
    }

    public String getCatagory(){
        return this.catagory;
    }

    public int compareTo(Bar other){
        if(other == null){
            throw new NullPointerException();
        }
        int otherval = other.getValue();
        int thisval = this.getValue();

        if(otherval > thisval) { return 1; }
        if(thisval > otherval) { return -1; }
        return 0;
    }
}
