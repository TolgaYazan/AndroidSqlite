package com.example.cmpe408assignment2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private EditText myFacultieInput,myDepartmentInput,myLectureInput, myNameInput, mySurnameInput; // inputs
    private TextView myTextStudentId, mySelectedFacultie;
    private TabHost tabhost; // student object
    private String tmpGender;
    Random rand = new Random();
    private ListView myListView,myListViewAdminstration,myListViewStudents; // listView
    static int studentListid;
    private Spinner myAdminstrationSpinner, myStudentSpinner;
    boolean checked;
    private SQLiteDatabase db;
    private String adminFacultie;
    private String   adminDepartment;
    private String adminLecturer ;
    private String selectedStudent ;
    String selectedStudentId;
    String selectedStudentName;
    String selectedStudentSurname;
    String selectedStudentGender;
    String selectedStudentFacultie;
    String selectedStudentDepartment;
    String selectedStudentLecturer;
    String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextStudentId = findViewById(R.id.textViewStudentId);

        myFacultieInput=findViewById(R.id.editFacultie); // to get facultie
        myDepartmentInput=findViewById(R.id.editDepartment); // to get department
        myLectureInput=findViewById(R.id.editLecturer); // to get lecturer

        myAdminstrationSpinner=findViewById(R.id.spinner_adminstration); // will be used in tab2 to show adminstration table by spinner
        myStudentSpinner=findViewById(R.id.spinner_students);// will be used in tab1 to show student table by spinner

        myNameInput=findViewById(R.id.editName); // to get name
        mySurnameInput=findViewById(R.id.editSurname); // to get surname

        mySelectedFacultie=findViewById(R.id.textViewSelectFacultie);

        myListView = findViewById(R.id.list_view); // student names will be show in tab3 by this listview
        myListViewAdminstration=findViewById(R.id.list_viewAdminstration); // admisntration table contents will be show in adminstration tab by this listview
        myListViewStudents=findViewById(R.id.listviewStudents); // student table contents will be show in studenttable by this listview

        File myDbPath = getApplication().getFilesDir(); // to get path
        path = myDbPath+"/"+"Cmpe412"; // to set path

        try {
            if (!databaseExist()) {
                // create database
                db =SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);

                //create administration table
                String table ="create table  administration ("
                        + "recID integer PRIMARY KEY autoincrement, "
                        + "facultie text, "
                        + "department text, "
                        + "lecturer text);";

                // execute the script to create the table
                db.execSQL(table); // we habe student table

                //create student table
                String tableStudent ="create table  student ("
                        + "recIDstuent integer PRIMARY KEY autoincrement, "
                        + "studentId text, "
                        + "name text, "
                        + "surname text, "
                        + "gender text, "
                        + "facultie text, "
                        + "department text, "
                        + "lecturer text);";

                // execute the script to create the table
                db.execSQL(tableStudent); // we habe student table
                Toast.makeText(this, "have the table2", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // setting up Tabhost selector
        tabhost = (TabHost) findViewById(android.R.id.tabhost);
        tabhost.setup();
        TabHost.TabSpec tabspec;

        // Setting first tab and tab1.xml adminstraion tab
        tabspec = tabhost.newTabSpec("screen1");
        tabspec.setContent(R.id.tab1);
        tabspec.setIndicator("administration", null);
        tabhost.addTab(tabspec);

        // Setting second tab and tab2.xml student tab
        tabspec = tabhost.newTabSpec("screen2");
        tabspec.setContent(R.id.tab2);
        tabspec.setIndicator("Registration", null);
        tabhost.addTab(tabspec);

        // Setting third tab and tab3.xml registeted student
        tabspec = tabhost.newTabSpec("screen3");
        tabspec.setContent(R.id.tab3);
        tabspec.setIndicator("Registered students", null);
        tabhost.addTab(tabspec);
        onListStudentsTab1(); // to list Registration on adminstration tab by spinner
        tabhost.setCurrentTab(0);

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            // defined what happend when we moves different tabs at the beginning
            public void onTabChanged(String tagId) {
                switch (tabhost.getCurrentTab()) {
                    case 0: // do something for tab-1
                        hideVirtualKeyboard(); // hide keyboard when  move to first tab
                        onListStudentsTab1(); // to list Registration on adminstration tab by spinner(myStudentSpinner)
                        break;
                    case 1: // do something for tab-2
                        hideVirtualKeyboard(); // hide keyboard when  move to second tab
                        myTextStudentId.setText(generateStudentID()); // generate student number randomly
                        onListAdminstraion(); // list admistration on tab 2 show in spinner ( myAdminstrationSpinner)
                        break;
                    case 2: // do something for tab-3
                        hideVirtualKeyboard();// hide keyboard when  move to third tab
                        onListStudent(); // list student on tab3 in liewView
                        break;
                }
            }
        });

        myListView.setOnItemClickListener(this); // to set tab3 list view will bu used in show student names and ids

        //  adminstration table will be show in  Registration tab in spinner  (myAdminstrationSpinner)
        myAdminstrationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {
                String selectedAdmin=myAdminstrationSpinner.getSelectedItem().toString(); // i get clicked item
                String tmp[] = selectedAdmin.split(" "); // i split items  and ı will use these items to register students
                adminFacultie=tmp[0]; // facultie store in adminlecture
                adminDepartment=tmp[1]; // department in adminDeparment
                adminLecturer=tmp[2]; // lecturer in admin lecturer
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }
    // end of the oncreate

    private boolean databaseExist(){ // checking database is exists
        File fbFile =new File(path);
        return fbFile.exists();// return true if exist
    }

    //myListView on registered student will list student id and student name
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String selectedStudentTmp = myListView.getItemAtPosition(i).toString(); // to get clicked student

        String tmp[] = selectedStudentTmp.split(" ");  // split student id and student name
        selectedStudent=tmp[1]; // student name stored in selected student will used in showmyInoRegisteredStudent method to show student details
        showMyInfoRegisteredStudent(this);
        Toast.makeText(getApplicationContext(), selectedStudent, Toast.LENGTH_SHORT).show();
        // If you want to close the adapter
    }

    // Show the selected students info on tab3 by using dialog messabe box
    private void showMyInfoRegisteredStudent(MainActivity mainActivity) { // selectedStudent name coming from listView in tab3(registeredStudent) method onitemclik lookt at 186 line
        String read = "select * from student where name = '"+selectedStudent+"'"; // ı get selected students by his name

        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor cursor=db.rawQuery(read,null);
        while(cursor.moveToNext()) {
            selectedStudentId = cursor.getString(1);// studentId
             selectedStudentName= cursor.getString(2);// studentName
            selectedStudentSurname = cursor.getString(3);// studentSurname
            selectedStudentGender = cursor.getString(4);// studentGender
             selectedStudentFacultie = cursor.getString(5);// studentFacultie
             selectedStudentDepartment = cursor.getString(6);// studentDepartment
            selectedStudentLecturer = cursor.getString(7);// studentLectuer

        } // setting mesage diaglog
        new AlertDialog.Builder(mainActivity)
                .setTitle("Registered Student")
                .setMessage("Id :" +selectedStudentId+"\n"+
                        "Name :" +selectedStudentName+ "\n"+
                        "Surname: "+selectedStudentSurname+"\n"+
                        "Gender: "+selectedStudentGender +"\n"+
                        "SelectedFacultie: "+selectedStudentFacultie+"\n"+
                        "SelectedDeparment: "+selectedStudentDepartment+"\n"+
                        "SelectedLecturer: "+selectedStudentLecturer)
                .setPositiveButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).create()
                .show();
    }

    // to list student on tab3(registered student) to show student id and name on tab3
    public void onListStudent(){
        String read = "select * from student"; // getting students
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor cursor=db.rawQuery(read,null);

        ArrayList <String> registeredStudents = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,registeredStudents);
        //read data from the record

        while(cursor.moveToNext()){
            String studentId = cursor.getString(1);// getting  studentid
            String studentName = cursor.getString(2);//  getting studentname

            //String surname =cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String result =studentId+" "+studentName ;
            registeredStudents.add(result);

        }// end of the while loop
        //display the result on the list View
        myListView.setAdapter(adapter);
        db.close();

    }

    // to list admintration table contents in tab 2(registration)  by spinner myAdminstrationSpinner
    public void onListAdminstraion(){ //will be used in onTabChanged method
        String read = "select * from administration";
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor cursor=db.rawQuery(read,null);

        ArrayList <String> administrationList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,administrationList);
        //read data from the record

        while(cursor.moveToNext()){
            String facultie = cursor.getString(1);// facultie
            String department = cursor.getString(2);// department
            String lecturer = cursor.getString(3);// lecturer
            //String surname =cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String result =facultie+" "+department +" "+lecturer;
            administrationList.add(result);

        }// end of the while loop
        //display the result on the list View
        myAdminstrationSpinner.setAdapter(adapter);
        db.close();
    }

    // to list students table contents in tab 1(admistration)  by spinner myStudentSpinner
    public void onListStudentsTab1(){
        String read = "select * from student";
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor cursor=db.rawQuery(read,null);

        ArrayList <String> administration = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,administration);
        //read data from the record

        while(cursor.moveToNext()){
            String studentId = cursor.getString(1);// facultie
            String studentName = cursor.getString(2);// department
            String studentSurname = cursor.getString(3);// lecturer
            String studentGender = cursor.getString(4);// facultie
            String studentFacultie = cursor.getString(5);// department
            String studentDepartment = cursor.getString(6);// lecturer
            String studentLecturer = cursor.getString(7);// lecturer
            //String surname =cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String result =studentId+" "+studentName +" "+studentSurname+" "+studentGender+" "+studentFacultie +" "+studentDepartment+" "+studentLecturer;
            administration.add(result);

        }// end of the while loop
        //display the result on the list View
        myStudentSpinner.setAdapter(adapter);
        db.close();
    }

    // hidekeyboard property when we changes tabs keyboard close
    public void hideVirtualKeyboard() {
        // temporarily remove the virtual keyboard
        ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    // Generate Randomly 10 disit number
    public String generateStudentID() {
        String result = "";
        int tmpint2 = 10000 + rand.nextInt(90000);
        int tmpint = 10000 + rand.nextInt(90000);
        String tmp2 = String.valueOf(tmpint);
        String tmp1 = String.valueOf(tmpint2);
        result = tmp2 + tmp1;
        return result;
    }

    // Radio Button Check operation
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
         checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioMale:
                if (checked)
                    tmpGender="Male";
                    break;
            case R.id.radioFemale:
                if (checked)
                    tmpGender="Female";
                    break;
        }
    }

    // creating option menü
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMyHelperMenu(menu);
        return true;
    }

    // proivde the selected item in opton menü
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuId = item.getItemId();
        if (selectedMenuId == 1) {
            showMyInfoAdminstrationDialog(this); // show dialag info Adminstration tab
            return true;
        } else if (selectedMenuId == 2) {
            showMyInfoRegistrationDialog(this); // show dialag info Registration tab
            return true;
        } else if (selectedMenuId == 3) {
            showMyInfoRegisteredStudentsDialog(this); // show dialag info Registrated Students tabs
            return true;
        }
        else if (selectedMenuId == 4) {
            showMyInfoRegistrationDialogCustom(this); // show dialag info Registrated Students tabs
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Populating option menu with helper method
    private void populateMyHelperMenu(Menu menu) {
        int groupId = 0;
        menu.add(groupId, 1, 1, "Info Adminstration"); // set id and name for option menü first item
        menu.add(groupId, 2, 2, "Info Registration"); // set id and name for option menü second item
        menu.add(groupId, 3, 3, "Info Registered Students");
        menu.add(groupId, 4, 4, "Custom Dialog");// set id and name for option menü third item
    }

    // Creating dialogbox to show info about Adminstration tab will be used in selected menu item
    private void showMyInfoAdminstrationDialog(MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Adminstration Tab")
                .setMessage("Adminstration tab is used to get general information\n" +
                        "of a university")
                .setPositiveButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).create()
                            .show();
    }

    // Creating dialogbox to show info about Register tab will be used in selected menu item
    private void showMyInfoRegistrationDialog(MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Registration Tab")
                .setMessage("It is a page made to register students. The number of students is \n" +
                        "generated automatically. In order for the students to register. \n" +
                        "The user must fill in the name, surname, gender parts and choose one from \n" +
                        "the list of faculty, department and teacher.")
                .setPositiveButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).create()
                .show();
    }
    private void showMyInfoRegistrationDialogCustom(MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Registration Tab")
                .setMessage("Deneme")
                .setPositiveButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).create()
                .show();
    }

    // Creating dialogbox to show info about Registered Students  tab will be used in selected menu item
    private void showMyInfoRegisteredStudentsDialog(MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Registered Students Tab")
                .setMessage("Displays all student that have been\n" +
                        "registered so far. When ever a student name is clicked on the list, all information \n" +
                        "of the student should be displayed within a pop up frame")
                .setPositiveButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).create()
                .show();
    }

    // will be used when we register student give info to user said thaat given student registered
    private void showRegisteredStudentDialog(MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Student succefully registered")
                .setMessage("Student "+ myNameInput.getText().toString()+ " registered" )
                .setPositiveButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).create()
                .show();
    }

    // to add faculties department and lecturers to adminstraiton table
    public void onAddTab1(View view) {
        if(myFacultieInput.length()<=0){
            Toast.makeText(getApplicationContext(), "Pls Enter a facultie ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (myDepartmentInput.length() <= 0) {
            Toast.makeText(getApplicationContext(), "Pls Enter a Department ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (myLectureInput.length() <= 0) {
            Toast.makeText(getApplicationContext(), "Pls Enter a Lecturer ", Toast.LENGTH_SHORT).show();
            return;
        }
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        String facultie = myFacultieInput.getText().toString();
        String department = myDepartmentInput.getText().toString();
        String lecturer = myLectureInput.getText().toString();
        // insert given valeu into admiistration table
        String input = "insert into administration (facultie,department,lecturer) values ('"+facultie+"','"+department+"','"+lecturer+"') ";
        db.execSQL(input);
        Toast.makeText(getApplication(),"data is saved",Toast.LENGTH_SHORT).show();

        myLectureInput.setText("");
        myFacultieInput.setText("");
        myDepartmentInput.setText("");
        db.close();
    }

    // to delete admistration table row by given name
    public void onDeleteTab1(View view){
        if(myFacultieInput.length()<=0){
            Toast.makeText(getApplicationContext(), "Pls use facultie name to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        String facultie = myFacultieInput.getText().toString().trim();
        String delete ="delete from administration where facultie = '"+facultie+"'"; // to delete row by given name
        db.execSQL(delete);
        Toast.makeText(getApplication(),facultie+"is deleted",Toast.LENGTH_LONG).show();
        myFacultieInput.setText("");
        myDepartmentInput.setText("");
        myLectureInput.setText("");
        db.close();
    }

    // to update admistration table row department by given facultie name
    public void onUpdateTab1(View view){
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        String facultie = myFacultieInput.getText().toString().trim();
        String department = myDepartmentInput.getText().toString().trim();
        String strSQL = "UPDATE administration SET department = '"+department+"' WHERE facultie =  '"+facultie+"'";
        db.execSQL(strSQL);
        Toast.makeText(getApplication(),facultie+"is update",Toast.LENGTH_LONG).show();
        myFacultieInput.setText("");
        myDepartmentInput.setText("");
        myLectureInput.setText("");
        db.close();
        return;
    }

    // to list admistration table contens  myListViewAdminstration in tab1 (adminstration)
    public void onSearchTab1(View view){
        String read = "select * from administration"; // gel all admistratio table contens
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor cursor=db.rawQuery(read,null);

        ArrayList <String> administration = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,administration);
        //read data from the record

        while(cursor.moveToNext()){
            String facultie = cursor.getString(1);// facultie
            String department = cursor.getString(2);// department
            String lecturer = cursor.getString(3);// lecturer
            //String surname =cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String result =facultie+" "+department +" "+lecturer;
            administration.add(result);

        }// end of the while loop
        //display the result on the list View
        myListViewAdminstration.setAdapter(adapter);
        db.close();

    }

    // to store student in student table ı will use adminfaulcite,adminsfacultie, adminlecturer
    //adminfaulcite,adminsfacultie, adminlecturer produced in myAdminstrasionSpinner look at line 163
    public void onRegisterStudent(View view){

        myTextStudentId.setText(generateStudentID());// change generated student new one
        String studentId=generateStudentID();

        if(myNameInput.length()<=0) { // if no name input give toast message and return
            Toast.makeText(getApplicationContext(), "Pls Enter a name ", Toast.LENGTH_SHORT).show();
            return;
        }
        String nameData=myNameInput.getText().toString();

        if(mySurnameInput.length()<=0) { // if no surname input give toast message and return
            Toast.makeText(getApplicationContext(), "Pls Enter a Surname ", Toast.LENGTH_SHORT).show();
            return;
        }
        String surnameData=mySurnameInput.getText().toString();

        if(!checked) { // if gender is not cheked give toast message and return
            Toast.makeText(getApplicationContext(), "Pls Select Gender", Toast.LENGTH_SHORT).show();
            return;
        }
        String genderDate = tmpGender;

        // to insert student to student table
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        String input = "insert into  student (studentId,name,surname,gender,facultie,department,lecturer) values ('"+studentId+"','"+nameData+"','"+surnameData+"','"+genderDate+"'" +
                ",'"+adminFacultie+"','"+adminDepartment+"','"+adminLecturer+"') ";
        db.execSQL(input);
        Toast.makeText(getApplication(),"data is saved",Toast.LENGTH_SHORT).show();

        db.close();


        showRegisteredStudentDialog(this); // meessage box when user click the add buton give to user info that student registered

        myNameInput.setText("");
        mySurnameInput.setText("");
    }

    // to delete students row by given student name
    public void onCancelStudentTab2(View view){
        if(myNameInput.length()<=0){
            Toast.makeText(getApplicationContext(), "Pls use  name to delete student", Toast.LENGTH_SHORT).show();
            return;
        }
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        String nameDelete = myNameInput.getText().toString().trim();
        // to delete students row by given student name
        String delete ="delete from student where name = '"+nameDelete+"'";
        db.execSQL(delete);
        Toast.makeText(getApplication(),nameDelete+"is deleted",Toast.LENGTH_LONG).show();
        myNameInput.setText("");
        mySurnameInput.setText("");
        db.close();

    }

    // to update studename surname by given name
    public void onUpdateStudentTab2(View view){
        if(myNameInput.length()<=0){
            Toast.makeText(getApplicationContext(), "Pls use  name to update", Toast.LENGTH_SHORT).show();
            return;
        }
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        String name = myNameInput.getText().toString().trim();
        String surname = mySurnameInput.getText().toString().trim();
        String strSQL = "UPDATE student SET surname = '"+surname+"' WHERE name =  '"+name+"'";
        db.execSQL(strSQL);
        Toast.makeText(getApplication(),name+"is update",Toast.LENGTH_LONG).show();
        myNameInput.setText("");
        mySurnameInput.setText("");
        db.close();
        return;

    }

    // to list  student  table content in tab2(registration) will be listed in listview  (myListViewStudents)
    public void onSearchStudentTab2(View view){
        String read = "select * from student";
        db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor cursor=db.rawQuery(read,null);

        ArrayList <String> administration = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,administration);
        //read data from the record

        while(cursor.moveToNext()){
            String studentId = cursor.getString(1);// studentID
            String studentName = cursor.getString(2);// studentname
            String studentSurname = cursor.getString(3);// studentsurname
            String studentGender = cursor.getString(4);// studengender
            String studentFacultie = cursor.getString(5);// studentfacultie
            String studentDepartment = cursor.getString(6);// studentdepartment
            String studentLecturer = cursor.getString(7);// studentlecturer
            //String surname =cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String result =studentId+" "+studentName +" "+studentSurname+" "+studentGender+" "+studentFacultie +" "+studentDepartment+" "+studentLecturer;
            administration.add(result);

        }// end of the while loop
        //display the result on the list View
        myListViewStudents.setAdapter(adapter);
        db.close();

        return;
    }
}// end of the class


