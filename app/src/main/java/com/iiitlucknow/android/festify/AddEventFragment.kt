package com.iiitlucknow.android.festify

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.iiitlucknow.android.festify.viewModels.AddEventFragmentViewModel
import com.iiitlucknow.android.festify.data_classes.AddEventData
import com.iiitlucknow.android.festify.databinding.FragmentAddeventsBinding
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class AddEventFragment : Fragment() {
    private var _binding: FragmentAddeventsBinding? = null
    private val binding get() = _binding!!
    lateinit var vm: AddEventFragmentViewModel
    lateinit var add_msg: String
    lateinit var add_err_msg: String
    val REQUEST_CODE = 100
    lateinit var path: Uri
    lateinit var url:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddeventsBinding.inflate(inflater, container, false)
        vm = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(AddEventFragmentViewModel::class.java)
        vm.addResponse.observe(
            viewLifecycleOwner
        ) {
            if (it.isSuccessful) {
                try {
                    val jsonObject = JSONObject(Gson().toJson(it.body()))
                    add_msg = jsonObject.getString("message")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Toast.makeText(context, add_msg, Toast.LENGTH_LONG).show()
            } else {
                try {
                    val jObjError = JSONObject(it.errorBody()!!.string())
                    add_err_msg = jObjError.getString("message")
                    Toast.makeText(context, add_err_msg, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        initSpinner(binding.eventCategorySpinner, R.array.event_category)

        binding.eventDateEditText.setOnClickListener {
            showDatePicker(it as TextInputEditText)
        }
        binding.eventTimeEditText.setOnClickListener {
            showTimePicker(it as TextInputEditText)
        }
        binding.eventEndDateEditText.setOnClickListener {
            showDatePicker(it as TextInputEditText)
        }
        binding.eventEndTimeEditText.setOnClickListener {
            showTimePicker(it as TextInputEditText)
        }

        binding.eventImage.setOnClickListener {
            openGalleryForImage()
        }

        binding.submitEventBtn.setOnClickListener {
            /**
             * Get selected item in category spinner like this :
             * binding.eventCategorySpinner.selectedItem.toString()
             */
            vm.checkAddEvent(AddEventData(
                binding.eventNameEditText.text.toString().trim(),
                binding.eventCategorySpinner.selectedItem.toString(),
                binding.eventDateEditText.text.toString().trim(),
                binding.eventEndDateEditText.text.toString().trim(),
                binding.eventTimeEditText.text.toString().trim(),
                binding.eventEndTimeEditText.text.toString().trim(),
                binding.eventDescriptionEditText.text.toString().trim(),
                url
            ))

        }
        return binding.root
    }
    private fun config() {
        val appConfig: HashMap<String, String> = HashMap<String, String> ()
        appConfig["cloud_name"] = "dezs1agpn"
        appConfig["api_key"]="657298376448555"
        appConfig["api_secret"] = "MAmfrZubbbdkCQoNiLFB6vGNtCo"
        MediaManager.init(requireContext(), appConfig)
    }

    private fun showDatePicker(edittext: TextInputEditText) {
        val _calendar = Calendar.getInstance()
        val _year = _calendar.get(Calendar.YEAR)
        val _month = _calendar.get(Calendar.MONTH)
        val _day = _calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                edittext.setText("$day/$month/$year")
            },
            _year,
            _month,
            _day
        )
        datePicker.show()
    }

    private fun showTimePicker(edittext: TextInputEditText) {
        val _calendar = Calendar.getInstance()
        val _hour = _calendar.get(Calendar.HOUR_OF_DAY)
        val _minute = _calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                if (minute < 10) edittext.setText("$hour:0$minute")
                else edittext.setText("$hour:$minute")
            },
            _hour,
            _minute,
            false
        )
        timePicker.show()
    }

    private fun initSpinner(_spinner: Spinner, _arrayId: Int) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            _arrayId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            _spinner.adapter = adapter
        }
    }

    // TODO : Deprecated, use a better method for image selection

     private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            binding.eventImage.setImageURI(data?.data) // handle chosen image
            path =data?.data!!
            uploadToCloudinary(path)

        }
    }
     private fun uploadToCloudinary(filePath: Uri) {
        Log.d("A", "sign up uploadToCloudinary- ")
        MediaManager.get().upload(filePath).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                //url="Start"
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
               //url="Uploading"
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                url= resultData["url"].toString()
            }

            override fun onError(requestId: String?, error: ErrorInfo) {
              // url=("error " + error.description)
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo) {
               // url=("Reschedule " + error.description)
            }
        }).dispatch()
    }
}
