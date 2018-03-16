package Model;

import java.io.InputStream;

public class VariablesSistema {
    public float inc;
    public float percent;
    public InputStream imagen;

    public VariablesSistema(float _inc, float _percent, InputStream _imagen){
        this.inc = _inc;
        this.percent = _percent;
        this.imagen = _imagen;
    }
}
