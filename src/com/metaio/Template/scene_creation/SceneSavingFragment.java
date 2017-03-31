package com.metaio.Template.scene_creation;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.xcapade.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentSceneSavingListener} interface
 * to handle interaction events.
 * Use the {@link SceneSavingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SceneSavingFragment extends DialogFragment
{
    public static String TAG = "SceneSavingFragment";

    private OnFragmentSceneSavingListener mListener;

    public SceneSavingFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SceneSavingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SceneSavingFragment newInstance()
    {
        SceneSavingFragment fragment = new SceneSavingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initListener();

    }

    private void initListener()
    {
        try
        {
            mListener = (OnFragmentSceneSavingListener)getActivity();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentSceneSavingListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scene_saving, container, false);

        View confirmButton = rootView.findViewById(R.id.confirm_scene_saving);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onConfirmButton();
            }
        });

        View cancelBtn = rootView.findViewById(R.id.cancel_scene_saving);
        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onCancelButton();
            }
        });

        if (mListener != null)
        {
            // Initialize by the previous input value
            EditText name = (EditText) rootView.findViewById(R.id.edit_scene_name);
            EditText coin = (EditText) rootView.findViewById(R.id.edit_scene_coin);
            EditText descr = (EditText) rootView.findViewById(R.id.edit_scene_descr);
            EditText hint = (EditText) rootView.findViewById(R.id.edit_scene_hint);
            EditText puzzle = (EditText) rootView.findViewById(R.id.edit_scene_puzzle);
            EditText puzzlehint = (EditText) rootView.findViewById(R.id.edit_scene_puzzlehint);
            EditText puzzleanswer = (EditText) rootView.findViewById(R.id.edit_scene_puzzleanswer);

            String[] spotStoreData = mListener.onRestoreForm();

            name.setText(spotStoreData[0]);
            coin.setText(spotStoreData[1]);
            descr.setText(spotStoreData[2]);
            hint.setText(spotStoreData[3]);
            puzzle.setText(spotStoreData[4]);
            puzzlehint.setText(spotStoreData[5]);
            puzzleanswer.setText(spotStoreData[6]);
        }

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void onConfirmButton()
    {
        EditText name = (EditText)getView().findViewById(R.id.edit_scene_name);
        EditText coin = (EditText)getView().findViewById(R.id.edit_scene_coin);
        EditText descr = (EditText)getView().findViewById(R.id.edit_scene_descr);
        EditText hint = (EditText)getView().findViewById(R.id.edit_scene_hint);
        EditText puzzle = (EditText)getView().findViewById(R.id.edit_scene_puzzle);
        EditText puzzlehint = (EditText)getView().findViewById(R.id.edit_scene_puzzlehint);
        EditText puzzleanswer = (EditText)getView().findViewById(R.id.edit_scene_puzzleanswer);

        String nameStr = name.getText().toString().trim();
        String coinStr = coin.getText().toString().trim();
        String descStr = descr.getText().toString().trim();
        String hintStr = hint.getText().toString().trim();
        String puzzleStr = puzzle.getText().toString().trim();
        String puzzlehintStr = puzzlehint.getText().toString().trim();
        String puzzleanswerStr = puzzleanswer.getText().toString().trim();

        if (!nameStr.equalsIgnoreCase("") && !coinStr.equalsIgnoreCase("") && !descStr.equalsIgnoreCase("") && !hintStr.equalsIgnoreCase("") && !puzzleStr.equalsIgnoreCase("") && !puzzlehintStr.equalsIgnoreCase("") && !puzzleanswerStr.equalsIgnoreCase(""))
        {
            if (mListener != null)
            {
                mListener.onSceneSavingConfirmed(nameStr, coinStr, descStr, hintStr, puzzleStr, puzzlehintStr, puzzleanswerStr);
            }
        }

    }

    public void onCancelButton()
    {
        if (mListener != null)
        {
            mListener.onSceneSavingCanceled();
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentSceneSavingListener
    {
        void onSceneSavingConfirmed(String name, String coin, String descr, String hint, String puzzle, String puzzlehint, String puzzleanswer);
        void onSceneSavingCanceled();
        String[] onRestoreForm();
    }
}
