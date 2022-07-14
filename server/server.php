<?php
session_start();

// initializing variables
$firstname 	= "";
$lastname 	= "";
$username 	= "";
$email    	= "";
$country 		= "";
$gender 		= "";
$errors 		= array(); 

// connect to the database
$db = mysqli_connect('localhost', 'root', '', 'registration');

// REGISTER USER
if (isset($_POST['reg_user'])) {
  // receive all input values from the form
  $firstname = mysqli_real_escape_string($db, $_POST['firstname']);
  $lastname = mysqli_real_escape_string($db, $_POST['lastname']);
  $username = mysqli_real_escape_string($db, $_POST['username']);
  $email = mysqli_real_escape_string($db, $_POST['email']);
  $country = mysqli_real_escape_string($db, $_POST['country']);
  $gender=mysqli_real_escape_string($db, $_POST['gender']);
  $password_1 = mysqli_real_escape_string($db, $_POST['password_1']);
  $password_2 = mysqli_real_escape_string($db, $_POST['password_2']);

  // form validation: ensure that the form is correctly filled ...
  // by adding (array_push()) corresponding error unto $errors array


  // first check the database to make sure 
  // a user does not already exist with the same username and/or email
  $user_check_query = "SELECT * FROM users WHERE username='$username' OR email='$email' LIMIT 1";
  $result 					= mysqli_query($db, $user_check_query);
  $user 						= mysqli_fetch_assoc($result);
  
  if ($user) { // if user exists
    if ($user['username'] === $username) {
      array_push($errors, "Username already exists");
    }

    if ($user['email'] === $email) {
      array_push($errors, "email already exists");
    }
  }

  // Finally, register user if there are no errors in the form
  if (count($errors) == 0) {
  	$password = md5($password_1);//encrypt the password before saving in the database

  	$query = "INSERT INTO users (firstname, lastname, username, email, password, country, gender) 
  			  VALUES('".$firstname."', '".$lastname."', '".$username."', '".$email."', '".$password."', '".$country."', '".$gender."')";
  	mysqli_query($db, $query);
  	$_SESSION['username'] = $username;
  	$_SESSION['success'] = "You are now logged in";
    header('location: index.php');
    die();
  }
} 

// ... 

// ... 

// LOGIN USER
if (isset($_POST['login_user'])) {
    $username = mysqli_real_escape_string($db, $_POST['username']);
    $password = mysqli_real_escape_string($db, $_POST['password']);
  

  
    if (count($errors) == 0) {
        //$password = md5($password);
        $query = "SELECT * FROM users WHERE username='".$username."' AND password='".$password."'";
        $results = mysqli_query($db, $query);
        if (mysqli_num_rows($results) == 1) {
          $_SESSION['username'] = $username;
          $_SESSION['success'] 	= "You are now logged in";
          echo 'Welcome';
        } else {
            echo 'Wrong user or password!';
            array_push($errors, "Wrong username/password combination");
        }
     }
  }
  
  ?>
