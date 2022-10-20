package demo.invoice;

public class Invoice {

    private String invoiceNumber;
    private String storeId;
    private long created;
    private double totalAmount;
    private boolean valid;

    public Invoice() {
    }

    public Invoice(String invoiceNumber, String storeId, long created, double totalAmount, boolean valid) {
        this.invoiceNumber = invoiceNumber;
        this.storeId = storeId;
        this.created = created;
        this.totalAmount = totalAmount;
        this.valid = valid;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Invoice setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }

    public String getStoreId() {
        return storeId;
    }

    public Invoice setStoreId(String storeId) {
        this.storeId = storeId;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public Invoice setCreated(long created) {
        this.created = created;
        return this;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Invoice setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public boolean isValid() {
        return valid;
    }

    public Invoice setValid(boolean valid) {
        this.valid = valid;
        return this;
    }
}
