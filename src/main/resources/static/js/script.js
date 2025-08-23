$(function(){

// User Register validation

	var $userRegister=$("#userRegister");

	$userRegister.validate({

		rules:{
			name:{
				required:true,
				lettersOnly:true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNumber: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			password: {
				required: true,
				space: true

			},
			confirmPassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			},
			address: {
				required: true,
				all: true

			},

			city: {
				required: true,
				space: true

			},
			state: {
				required: true,


			},
			pinCode: {
				required: true,
				space: true,
				numericOnly: true

			}, img: {
				required: true,
			}

		},
		messages:{
			name:{
				required:'name required',
				lettersOnly:'invalid name'
			},
			email: {
				required: 'email name must be required',
				space: 'space not allowed',
				customEmail: 'Invalid email'
			},
			mobileNumber: {
				required: 'Mobile no must be required',
				space: 'space not allowed',
				numericOnly: 'invalid mobile no',
				minlength: 'min 10 digit',
				maxlength: 'max 12 digit'
			},

			password: {
				required: 'password must be required',
				space: 'space not allowed'

			},
			confirmPassword: {
				required: 'confirm password must be required',
				space: 'space not allowed',
				equalTo: 'password mismatch'

			},
			address: {
				required: 'address must be required',
				all: 'invalid'

			},

			city: {
				required: 'city must be required',
				space: 'space not allowed'

			},
			state: {
				required: 'state must be required',
				space: 'space not allowed'

			},
			pinCode: {
				required: 'pinCode must be required',
				space: 'space not allowed',
				numericOnly: 'invalid pinCode'

			},
			img: {
				required: 'image required',
			}
		}
	})


// Orders Validation

var $orders=$("#orders");
$orders.validate({
		rules:{
			firstName:{
				required:true,
				lettersOnly:true
			},
			lastName:{
				required:true,
				lettersOnly:true
			}
			,
			email: {
				required: true,
				space: true,
				customEmail: true
			},
			mobileNo: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			address: {
				required: true,
				all: true

			},

			city: {
				required: true,
				space: true

			},
			state: {
				required: true,


			},
			pinCode: {
				required: true,
				space: true,
				numericOnly: true

			},
			paymentType:{
			required: true
			}
		},
		messages:{
			firstName:{
				required:'first required',
				lettersOnly:'invalid name'
			},
			lastName:{
				required:'last name required',
				lettersOnly:'invalid name'
			},
			email: {
				required: 'email name must be required',
				space: 'space not allowed',
				customEmail: 'Please enter a valid email address.'
			},
			mobileNo: {
				required: 'Mobile no must be required',
				space: 'space not allowed',
				numericOnly: 'invalid mobile no',
				minlength: 'min 10 digit',
				maxlength: 'max 12 digit'
			}
		   ,
			address: {
				required: 'address must be required',
				all: 'invalid'

			},

			city: {
				required: 'city must be required',
				space: 'space not allowed'

			},
			state: {
				required: 'state must be required',
				space: 'space not allowed'

			},
			pinCode: {
				required: 'pinCode must be required',
				space: 'space not allowed',
				numericOnly: 'invalid pinCode'

			},
			paymentType:{
			required: 'select payment type'
			}
		}
})

// Reset Password Validation

var $resetPassword=$("#resetPassword");

$resetPassword.validate({

		rules:{
			password: {
				required: true,
				space: true

			},
			confirmPassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			}
		},
		messages:{
		   password: {
				required: 'password must be required',
				space: 'space not allowed'

			},
			confirmPassword: {
				required: 'confirm password must be required',
				space: 'space not allowed',
				equalTo: 'password mismatch'

			}
		}
})
})




// Use Regex for customise field validation.

jQuery.validator.addMethod('lettersOnly', function(value, element) {
		return /^[^-\s][a-zA-Z_\s-]+$/.test(value);
	});

		jQuery.validator.addMethod('space', function(value, element) {
		return /^[^-\s]+$/.test(value);
	});

	jQuery.validator.addMethod('all', function(value, element) {
		return /^[^-\s][a-zA-Z0-9_,.\s-]+$/.test(value);
	});


	jQuery.validator.addMethod('numericOnly', function(value, element) {
		return /^[0-9]+$/.test(value);
	});

	// Custom Email Validation Method
    jQuery.validator.addMethod("customEmail", function (value, element) {
        return this.optional(element) || /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value);
    },);


