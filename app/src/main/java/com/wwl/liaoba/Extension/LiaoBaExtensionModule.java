package com.wwl.liaoba.Extension;

import android.view.KeyEvent;
import android.widget.EditText;

import com.wwl.liaoba.plugin.ConectionPlugin;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.EmojiTab;
import io.rong.imkit.emoticon.IEmojiItemClickListener;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

public class LiaoBaExtensionModule extends DefaultExtensionModule {
    private EditText mEditText;

    @Override
    public void onAttachedToExtension(RongExtension extension) {
        super.onAttachedToExtension(extension);
        mEditText = extension.getInputEditText();
    }

    @Override
    public void onDetachedFromExtension() {
        super.onDetachedFromExtension();
        mEditText = null;
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = super.getPluginModules(conversationType);
        //去掉音视频plugin
//        for (IPluginModule module : pluginModules) {
//            if (module instanceof FilePlugin) {
//                pluginModules.remove(module);
//            }
//
//        }
        //去掉"位置"plugin
        //注意：在删除CombineLocationPlugin的时候如果用上面增强for循环会抛异常java.util.ConcurrentModificationException，所以改成这种方式遍历删除
//        ListIterator<IPluginModule> iterator = pluginModules.listIterator();
//        while (iterator.hasNext()) {
//            IPluginModule integer = iterator.next();
//            if (integer instanceof CombineLocationPlugin) {
//                iterator.remove();
//            }
//        }
        pluginModules.add(new ConectionPlugin());
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        List<IEmoticonTab> list = new ArrayList<>();
        EmojiTab emojiTab = new EmojiTab();
        emojiTab.setOnItemClickListener(new IEmojiItemClickListener() {
            @Override
            public void onEmojiClick(String emoji) {
                int start = mEditText.getSelectionStart();
                mEditText.getText().insert(start, emoji);
            }

            @Override
            public void onDeleteClick() {
                mEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        });

        list.add(new LiaoBaEmoticonTab());
        list.add(emojiTab);
        return list;
    }
}
