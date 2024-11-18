package gg.vexi.Puppeteer.Exceptions;

public class PuppetNotFound extends RuntimeException {

    public PuppetNotFound() {
        super();
    }

    public PuppetNotFound(String message) {
        super(message);
    }

    public PuppetNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
