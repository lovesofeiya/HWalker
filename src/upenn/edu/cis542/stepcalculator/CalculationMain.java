package upenn.edu.cis542.stepcalculator;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class CalculationMain extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculation_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calculation_main, menu);
        return true;
    }
    
}
