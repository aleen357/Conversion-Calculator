package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Spinner source;
    Spinner destination;
    String[] temperature={"Celsius", "Kelvin", "Fahrenheit"};
    String[] weight={"pound","ounce", "ton","g", "kg"};
    String[] length={"inch", "foot", "yard","mile","cm","km"};
    String[] units={"inch", "foot", "yard","mile", "cm","km","pound","ounce", "ton","kg","g", "Celsius", "Kelvin", "Fahrenheit"};//defining all units
    String[] smallerUnits={};
    String[] largerUnits={};
    String selectedSourceUnit;
    String selectedDestinationUnit;
    Button convert;
    Map<String, Double> lengthConversion;//hashmaps to store all length units to their cm conversion
    Map<String, Double> weightConversion;//hashmaps to store all weight units to their g conversion
    Double answer;
    EditText sourceValue;
    TextView destinationValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        source=findViewById(R.id.dropdownSource);
        destination=findViewById(R.id.dropdownDestination);
        convert=findViewById(R.id.button);

        sourceValue=findViewById(R.id.sourceInput);

        destinationValue=findViewById(R.id.destinationInput);

        LengthConversion();//initialize a length hashmap
        WeightConversion();//initializes a weight hashmap

        ArrayAdapter<String> adapter= new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        source.setAdapter(adapter);
        destination.setAdapter(adapter);

        //two limit answers to 6 decimal places
        DecimalFormat df = new DecimalFormat("#.######");
        df.setRoundingMode(RoundingMode.CEILING);


        //for source spinner menu
        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSourceUnit=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //for destination spinner value
        destination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDestinationUnit=parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //function when we press convert button
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int set=0;


                //exception in case of null or . as an input only
                if (sourceValue.getText().toString().equals("") || sourceValue.getText().toString().equals("."))
                {
                    Toast.makeText(getApplicationContext(), "Enter Correct Value!",
                            Toast.LENGTH_LONG).show();
                    destinationValue.setText("");
                    return;
                }
                //exception if the input is not a number
                if (!isNumeric()) {
                    Toast.makeText(MainActivity.this, "Invalid Input!Not a Number", Toast.LENGTH_SHORT).show();
                    destinationValue.setText("");
                    return;
                }


                //first we see if the source and destination were from the same set
                if (Arrays.asList(temperature).contains(selectedSourceUnit) && Arrays.asList(temperature).contains(selectedDestinationUnit)) {
                    set=1;//this tells it is in temperature
                } else if (Arrays.asList(weight).contains(selectedSourceUnit) && Arrays.asList(weight).contains(selectedDestinationUnit)) {
                    set=2;//this tells it is in weight
                } else if (Arrays.asList(length).contains(selectedSourceUnit) && Arrays.asList(length).contains(selectedDestinationUnit)) {
                    set=3;//this tells it is in length
                } else {
                    Toast.makeText(MainActivity.this, "Wrong units Selected!", Toast.LENGTH_SHORT).show();
                    destinationValue.setText("");
                    return;
                }


                switch (set){
                    case 1:
                        if(!TemperatureRangeValidator())//this range validator has checks if temperature is in the range
                        {
                            destinationValue.setText("");
                            return;
                        }
                        answer=TemperatureConverter();
                        break;
                    case 2:
                        if(!WeightAndLengthValidator())//check if input weight or length was negative
                        {
                            destinationValue.setText("");
                            return;
                        }
                        answer=WeightConverter();
                        break;
                    case 3:
                        if(!WeightAndLengthValidator())//check if input weight or length was negative
                        {
                            destinationValue.setText("");
                            return;
                        }
                        answer=LengthConverter();
                        break;
                    default:

                }
                Toast.makeText(MainActivity.this, answer.toString(), Toast.LENGTH_SHORT).show();
                destinationValue.setText((df.format(answer)).toString());
                answer=0.0;

                return;


            }
        });



    }
    public void LengthConversion() {//these all provide a multiplying factor which can convert length to cm
        lengthConversion = new HashMap<>();
        lengthConversion.put("inch", 2.54);
        lengthConversion.put("foot", 30.48);
        lengthConversion.put("yard", 91.44);
        lengthConversion.put("mile", 1.60934);
        lengthConversion.put("km", 100000.0);
        lengthConversion.put("cm", 1.0);


    }
    public void WeightConversion() {//we have standardized all weight conversions to g first
        weightConversion = new HashMap<>();
        weightConversion.put("pound", 453.592);
        weightConversion.put("ounce", 30.48);
        weightConversion.put("ton", 1000000.0);
        weightConversion.put("kg", 1000.0);
        weightConversion.put("g", 1.0);

    }
    public boolean WeightAndLengthValidator()
    {
        //checks that weight and length should not be negative
        Double input = Double.parseDouble(sourceValue.getText().toString());

        if (input<0) {
            Toast.makeText(MainActivity.this, "Length and weight cannot be negative", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }
    public boolean TemperatureRangeValidator()
    {
        Double input = Double.parseDouble(sourceValue.getText().toString());

        if (Double.isNaN(input) || Double.isInfinite(input)) {
            Toast.makeText(MainActivity.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (input<0 && (selectedSourceUnit.equals("Kelvin"))) {
            Toast.makeText(MainActivity.this, "Kelvin cannot be negative", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedSourceUnit.equals("Celsius") || selectedSourceUnit.equals("Fahrenheit")) {
            if (input < -273.15) {
                Toast.makeText(MainActivity.this, "Temperature cannot be below absolute zero.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (selectedSourceUnit.equals("Fahrenheit") && (input < -459.67 || input > 212)) {
            Toast.makeText(MainActivity.this, "Temperature must be in the range of -459.67°F to 212°F.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedSourceUnit.equals("Celsius") && (input < -273.15 || input > 100)) {
            Toast.makeText(MainActivity.this, "Temperature must be within the range of -273.15°C to 100°C.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public Double TemperatureConverter(){
        if(Objects.equals(selectedSourceUnit, "Fahrenheit") && Objects.equals(selectedDestinationUnit, "Celsius"))
        {
            return fahrenheitToCelsius();
        }
        else if(Objects.equals(selectedSourceUnit, "Celsius") && Objects.equals(selectedDestinationUnit, "Fahrenhite"))
        {
            return celsiusToFahrenheit();
        }
        else if(Objects.equals(selectedSourceUnit, "Celsius") && Objects.equals(selectedDestinationUnit, "Kelvin"))
        {
            return celsiusToKelvin();
        }
        else if(Objects.equals(selectedSourceUnit, "Kelvin") && Objects.equals(selectedDestinationUnit, "Celsius"))
        {
            return celsiusToFahrenheit();
        }
        else if(Objects.equals(selectedSourceUnit, "Fahrenheit") && Objects.equals(selectedDestinationUnit, "Kelvin"))
        {
            return celsiusToKelvin();
        }
        else if(Objects.equals(selectedSourceUnit, "Kelvin") && Objects.equals(selectedDestinationUnit, "Fahrenheit"))
        {
            return celsiusToFahrenheit();
        }



        return null;
    }
    public Double WeightConverter(){
        //first take factor from hashmap
        Double factor=weightConversion.get(selectedSourceUnit);
        //multiply the source input with this factor
         answer=Double.parseDouble(sourceValue.getText().toString());
        answer=answer*factor;

        //converted to grams
        //now to destination source unit conversion


        Double SecondFactor=weightConversion.get(selectedDestinationUnit);
        if(!Objects.equals(selectedDestinationUnit, "g"))
            answer=answer/SecondFactor;
        //destinationValue.setText(answer.toString());

        return answer;
    }
    public Double LengthConverter(){

        //first take factor from hashmap
        Double factor=lengthConversion.get(selectedSourceUnit);
        //multiply the source input with this factor
        answer=Double.parseDouble(sourceValue.getText().toString());
        answer=answer*factor;
        //converted to centimeter now back to destination unit

        Double SecondFactor=lengthConversion.get(selectedDestinationUnit);
        if(!Objects.equals(selectedDestinationUnit, "cm"))
            answer=answer/SecondFactor;
        //destinationValue.setText(answer.toString());

        return answer;
    }
    //All temperature conversion functions
    public  Double celsiusToFahrenheit() {
        Double celsius=Double.parseDouble(sourceValue.getText().toString());
        return (celsius * 1.8) + 32;
    }

    public  Double fahrenheitToCelsius() {
        Double fahrenheit=Double.parseDouble(sourceValue.getText().toString());

        return (fahrenheit - 32) / 1.8;
    }

    public  Double celsiusToKelvin() {
        Double celsius=Double.parseDouble(sourceValue.getText().toString());

        return celsius + 273.15;
    }
    public  Double kelvinToCelsius() {
        Double kelvin=Double.parseDouble(sourceValue.getText().toString());

        return kelvin - 273.15;
    }
    public Double fahrenheitToKelvin() {
        Double fahrenheit=Double.parseDouble(sourceValue.getText().toString());
        Double kelvin = ((fahrenheit - 32) * 5 / 9) + 273.15;
        return kelvin;
    }

    // Kelvin to Fahrenheit
    public Double kelvinToFahrenheit() {
        Double kelvin=Double.parseDouble(sourceValue.getText().toString());
        Double fahrenheit = ((kelvin - 273.15) * 9 / 5) + 32;
        return fahrenheit;
    }

    public boolean isNumeric() {
        try {
            Double.parseDouble(sourceValue.getText().toString());
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }


}