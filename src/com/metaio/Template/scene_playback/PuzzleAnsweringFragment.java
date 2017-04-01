package com.metaio.Template.scene_playback;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.superman.capade.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentPuzzleAnsweringListener} interface
 * to handle interaction events.
 * Use the {@link PuzzleAnsweringFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PuzzleAnsweringFragment extends DialogFragment
{
    public static String TAG = "PuzzleAnsweringFragment";

    private OnFragmentPuzzleAnsweringListener mListener;

    public PuzzleAnsweringFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PuzzleAnsweringFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PuzzleAnsweringFragment newInstance()
    {
        PuzzleAnsweringFragment fragment = new PuzzleAnsweringFragment();
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
            mListener = (OnFragmentPuzzleAnsweringListener)getActivity();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentPuzzleAnsweringListener");
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
        View rootView = inflater.inflate(R.layout.fragment_puzzle_answer, container, false);

        View confirmButton = rootView.findViewById(R.id.background_img);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onConfirmAnswer();
            }
        });

        if (mListener != null)
        {
            // Initialize by the previous input value
            TextView puzzleQuestion = (TextView) rootView.findViewById(R.id.puzzle_question);
            TextView puzzleHint = (TextView) rootView.findViewById(R.id.puzzle_hint);
            // EditText answer = (EditText) rootView.findViewById(R.id.edit_puzzleanswer);

            String[] puzzleStoreData = mListener.onRestoreForm();

            puzzleQuestion.setText(puzzleStoreData[0]);
            puzzleHint.setText("Hint:" + puzzleStoreData[1]);
            // answer.setText(puzzleStoreData[2]);
        }

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void onConfirmAnswer()
    {
        EditText answer = (EditText)getView().findViewById(R.id.edit_puzzleanswer);
        String answerStr = answer.getText().toString().trim();
        if (mListener != null)
        {
            if(answerStr.equals(""))
            {
                mListener.onPuzzleAnsweringCanceled();
            }
            else
            {
                mListener.onPuzzleAnsweringConfirmed(answerStr);
            }

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
    public interface OnFragmentPuzzleAnsweringListener
    {
        void onPuzzleAnsweringConfirmed(String answerStr);
        void onPuzzleAnsweringCanceled();
        String[] onRestoreForm();
    }
}
