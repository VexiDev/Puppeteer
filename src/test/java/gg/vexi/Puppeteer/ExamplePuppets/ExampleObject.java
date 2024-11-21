
package gg.vexi.Puppeteer.ExamplePuppets;

public class ExampleObject {

    public final String data;

    public ExampleObject(String data) {
        if ( data == null ) {
            this.data = "Null";
            return;
        }
        this.data = data;
    }
}
