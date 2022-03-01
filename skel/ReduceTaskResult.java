public class ReduceTaskResult {

    public String fileName;
    public float rang;
    public int maxLength;
    public int nrMaxWords;

    public ReduceTaskResult(String fileName, float rang, int maxLength, int nrMaxWords) {
        this.fileName = fileName;
        this.rang = rang;
        this.maxLength = maxLength;
        this.nrMaxWords = nrMaxWords;
    }
}
