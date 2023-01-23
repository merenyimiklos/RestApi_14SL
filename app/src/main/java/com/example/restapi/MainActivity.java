package com.example.restapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button buttonAdd;
    private Button buttonNew;
    private Button buttonEdit;
    private Button buttonBack;
    private EditText editTextName;
    private EditText editTextAge;
    private EditText editTextEmail;
    private EditText editTextId;
    private ListView listViewAdatok;
    private ProgressBar progressBar;
    private LinearLayout linearLayoutPersonForm;
    private List<Person> people = new ArrayList<>();
    private String url = "https://retoolapi.dev/WRX5kT/people";

    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        linearLayoutPersonForm.setVisibility(View.GONE);
        buttonEdit.setVisibility(View.GONE);
        RequestTask task = new RequestTask(url, "GET");
        task.execute();

        buttonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutPersonForm.setVisibility(View.VISIBLE);
                buttonNew.setVisibility(View.GONE);
            }
        });

        buttonBack.setOnClickListener(view -> {
            linearLayoutPersonForm.setVisibility(View.GONE);
            buttonNew.setVisibility(View.VISIBLE);
            urlapAlaphelyzetbe();
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emberHozzadas();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emberModositas();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RequestTask task = new RequestTask(url, "GET");
                task.execute();
                swipeRefreshLayout.setRefreshing(false);//EZ FONTOS ITT
            }
        });
    }

    private void emberHozzadas() {
        String name = editTextName.getText().toString();
        String ageText = editTextAge.getText().toString();
        String email = editTextEmail.getText().toString();

        boolean valid = validacio();
        if (valid) {
            Toast.makeText(this,
                    "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageText);
        Person person = new Person(0, name, email, age);
        Gson jsonConverter = new Gson();
        RequestTask task = new RequestTask(
                url, "POST", jsonConverter.toJson(person));
        task.execute();
    }

    private void emberModositas() {
        String name = editTextName.getText().toString();
        String ageText = editTextAge.getText().toString();
        String email = editTextEmail.getText().toString();
        String idText = editTextId.getText().toString();

        int age = Integer.parseInt(ageText);
        int id = Integer.parseInt(idText);

        boolean valid = validacio();

        if (valid) {
            Toast.makeText(this,
                    "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
            return;
        }

        Person person = new Person(id, name, email, age);
        Gson jsonConverter = new Gson();
        RequestTask task = new RequestTask(url + "/" + id, "PUT",
                jsonConverter.toJson(person));
        task.execute();
    }

    private boolean validacio() {
        if (editTextEmail.getText().toString().isEmpty() ||
                editTextAge.getText().toString().isEmpty() ||
                editTextName.getText().toString().isEmpty())
            return true;
        else
            return false;
    }

    public void init() {
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonNew = findViewById(R.id.buttonNew);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonBack = findViewById(R.id.buttonBack);

        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextEmail = findViewById(R.id.editTextEmail);

        progressBar = findViewById(R.id.progressBar);

        linearLayoutPersonForm = findViewById(R.id.linearLayoutPersonForm);

        listViewAdatok = findViewById(R.id.listViewAdatok);
        listViewAdatok.setAdapter(new PersonAdapter());

        swipeRefreshLayout = findViewById(R.id.refreshLayout);
    }

    private class PersonAdapter extends ArrayAdapter<Person> {

        public PersonAdapter() {
            super(MainActivity.this, R.layout.person_list_items, people);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.person_list_items, null, false);
            Person acTualPerson = people.get(position);
            TextView textViewName = view.findViewById(R.id.textViewName);
            TextView textViewAge = view.findViewById(R.id.textViewAge);
            TextView textViewModosit = view.findViewById(R.id.textViewUpdate);
            TextView textViewTorles = view.findViewById(R.id.textViewDelete);

            textViewName.setText(acTualPerson.getName());
            textViewAge.setText("(" + acTualPerson.getAge() + ")");

            textViewModosit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayoutPersonForm.setVisibility(View.VISIBLE);
                    editTextId.setText(String.valueOf(acTualPerson.getId()));
                    editTextEmail.setText(acTualPerson.getEmail());
                    editTextName.setText(acTualPerson.getName());
                    editTextAge.setText(String.valueOf(acTualPerson.getAge()));
                    buttonEdit.setVisibility(View.VISIBLE);
                    buttonAdd.setVisibility(View.GONE);
                    buttonNew.setVisibility(View.GONE);
                }
            });

            textViewTorles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestTask task = new RequestTask(url,
                            "DELETE", String.valueOf(acTualPerson.getId()));
                    task.execute();
                }
            });

            return view;
        }
    }

    private void urlapAlaphelyzetbe() {
        editTextAge.setText("");
        editTextName.setText("");
        editTextEmail.setText("");
        linearLayoutPersonForm.setVisibility(View.GONE);
        buttonNew.setVisibility(View.VISIBLE);
        buttonAdd.setVisibility(View.VISIBLE);
        buttonEdit.setVisibility(View.GONE);
        RequestTask task = new RequestTask(url, "GET");
        task.execute();
    }

    private class RequestTask extends AsyncTask<Void, Void, Response> {
        String requestUrl;
        String requestType;
        String requestParams;

        public RequestTask(String requestUrl, String requestType, String requestParams) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
            this.requestParams = requestParams;
        }


        public RequestTask(String requestUrl, String requestType) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
        }

        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestType) {
                    case "GET":
                        response = RequestHandler.get(requestUrl);
                        break;
                    case "POST":
                        response = RequestHandler.post(requestUrl, requestParams);
                        break;
                    case "PUT":
                        response = RequestHandler.put(requestUrl, requestParams);
                        break;
                    case "DELETE":
                        response = RequestHandler.delete(requestUrl + "/" + requestParams);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(MainActivity.this,
                        "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                Log.d("onPostExecuteError:", response.getContent());
            }
            switch (requestType) {
                case "GET":
                    Person[] peopleArray = converter.fromJson(
                            response.getContent(), Person[].class);
                    people.clear();
                    people.addAll(Arrays.asList(peopleArray));
                    break;
                case "POST":
                    Person person = converter.fromJson(
                            response.getContent(), Person.class);
                    people.add(0, person);
                    urlapAlaphelyzetbe();
                    break;
                case "PUT":
                    Person updatePerson = converter.fromJson(
                            response.getContent(), Person.class);
                    people.replaceAll(person1 ->
                            person1.getId() == updatePerson.getId() ? updatePerson : person1);
                    urlapAlaphelyzetbe();
                    break;
                case "DELETE":
                    int id = Integer.parseInt(requestParams);
                    people.removeIf(person1 -> person1.getId() == id);
                    break;
            }
        }
    }
}