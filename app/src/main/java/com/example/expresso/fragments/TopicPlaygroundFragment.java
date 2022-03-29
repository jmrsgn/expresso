package com.example.expresso.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.expresso.R;
import com.example.expresso.utils.TopicPlaygroundHandler;

import io.github.rosemoe.editor.langs.java.JavaLanguage;
import io.github.rosemoe.editor.widget.CodeEditor;
import io.github.rosemoe.editor.widget.EditorColorScheme;

public class TopicPlaygroundFragment extends Fragment {
    // constants
    private static final String TAG = "TopicPlaygroundFragment";
    private final Context mContext;

    // views
    private CodeEditor ceTopicPlaygroundFragmentEditor;
    private EditText etTopicPlaygroundFragmentInputs;
    private Button btnTopicPlaygroundFragmentRun, btnTopicPlaygroundFragmentVisualize, btnTopicPlaygroundFragmentClearInput;

    // variables
    private String templateCode =
            "public class SomeClassName {\n" +
            "     public static void main(String[] args){\n" +
            "         // write your codes here\n" +
            "     }\n" +
            " }";

    public TopicPlaygroundFragment(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_playground, container, false);

        initAll(view);

        btnTopicPlaygroundFragmentRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopicPlaygroundHandler.compileScript(mContext, ceTopicPlaygroundFragmentEditor.getText().toString());
            }
        });

        btnTopicPlaygroundFragmentVisualize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopicPlaygroundHandler.visualizeScript(mContext, ceTopicPlaygroundFragmentEditor.getText().toString());
            }
        });

        btnTopicPlaygroundFragmentClearInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etTopicPlaygroundFragmentInputs.setText("");
            }
        });


        return view;
    }

    private void initAll(View view) {
        ceTopicPlaygroundFragmentEditor = view.findViewById(R.id.ce_topic_playground_fragment_editor);
        btnTopicPlaygroundFragmentRun = view.findViewById(R.id.btn_topic_playground_fragment_run);
        btnTopicPlaygroundFragmentVisualize = view.findViewById(R.id.btn_topic_playground_fragment_visualize);
        btnTopicPlaygroundFragmentClearInput = view.findViewById(R.id.btn_topic_playground_fragment_clear_input);
        etTopicPlaygroundFragmentInputs = view.findViewById(R.id.et_topic_playground_fragment_inputs);

        ceTopicPlaygroundFragmentEditor.setEditorLanguage(new JavaLanguage());
        EditorColorScheme editorColorScheme = new EditorColorScheme();
        editorColorScheme.setColor(1, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(2, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(3, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(4, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(5, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(6, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(7, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(8, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(9, getResources().getColor(R.color.cb_gray_600));
        editorColorScheme.setColor(10, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(11, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(12, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(13, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(14, getResources().getColor(R.color.cb_gray_600));
        editorColorScheme.setColor(15, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(16, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(17, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(18, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(19, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(20, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(21, getResources().getColor(R.color.cb_light_blue));
        editorColorScheme.setColor(22, getResources().getColor(R.color.cb_gray_400));
        editorColorScheme.setColor(23, getResources().getColor(R.color.cb_pink));
        editorColorScheme.setColor(24, getResources().getColor(R.color.cb_purple));
        editorColorScheme.setColor(25, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(26, getResources().getColor(R.color.cb_yellow_green));
        editorColorScheme.setColor(27, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(28, getResources().getColor(R.color.cb_pink));
        editorColorScheme.setColor(29, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(30, getResources().getColor(R.color.cb_on_background_black));

        Typeface tfConsolasRegular = ResourcesCompat.getFont(mContext, R.font.consolas_regular);

        ceTopicPlaygroundFragmentEditor.setColorScheme(editorColorScheme);
        ceTopicPlaygroundFragmentEditor.setTextSize(14);
        ceTopicPlaygroundFragmentEditor.setTypefaceText(tfConsolasRegular);

        setValues();
    }

    private void setValues() {
        ceTopicPlaygroundFragmentEditor.setText(templateCode);
    }
}

