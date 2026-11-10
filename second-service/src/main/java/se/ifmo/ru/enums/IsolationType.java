package se.ifmo.ru.enums;

public enum IsolationType {
    READ_COMMITTED("READ COMMITTED"),
    REPEATABLE_READ("REPEATABLE READ"),
    SERIALIZABLE("SERIALIZABLE");
    private final String type;
    IsolationType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}