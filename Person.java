public abstract class Person {
    protected int id;
    protected String name;

    public Person(int id, String name) {
        this.id = id;
        this.name = name != null ? name.trim() : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public abstract String getRole();

    public abstract String getDisplayDetails();

    @Override
    public String toString() {
        return getRole() + " [ID=" + id + ", Name=" + name + "]";
    }
}

