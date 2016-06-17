// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingsActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.SettingsActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558431, "field '_declineResponseTextView'");
    target._declineResponseTextView = finder.castView(view, 2131558431, "field '_declineResponseTextView'");
    view = finder.findRequiredView(source, 2131558430, "field '_declineResponseEditText'");
    target._declineResponseEditText = finder.castView(view, 2131558430, "field '_declineResponseEditText'");
    view = finder.findRequiredView(source, 2131558429, "field '_acceptResponseTextView'");
    target._acceptResponseTextView = finder.castView(view, 2131558429, "field '_acceptResponseTextView'");
    view = finder.findRequiredView(source, 2131558428, "field '_acceptResponsetEditText'");
    target._acceptResponsetEditText = finder.castView(view, 2131558428, "field '_acceptResponsetEditText'");
    view = finder.findRequiredView(source, 2131558480, "field 'textMessageCheckBox'");
    target.textMessageCheckBox = finder.castView(view, 2131558480, "field 'textMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558478, "field 'cloudMessageCheckBox'");
    target.cloudMessageCheckBox = finder.castView(view, 2131558478, "field 'cloudMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558477, "field 'selectAllowSaveCheckBox'");
    target.selectAllowSaveCheckBox = finder.castView(view, 2131558477, "field 'selectAllowSaveCheckBox'");
    view = finder.findRequiredView(source, 2131558479, "field 'selectFindFriendsCheckBox'");
    target.selectFindFriendsCheckBox = finder.castView(view, 2131558479, "field 'selectFindFriendsCheckBox'");
    view = finder.findRequiredView(source, 2131558427, "field 'currentPhoneNumberTextView'");
    target.currentPhoneNumberTextView = finder.castView(view, 2131558427, "field 'currentPhoneNumberTextView'");
    view = finder.findRequiredView(source, 2131558496, "field 'updateCurrentNumberButton'");
    target.updateCurrentNumberButton = finder.castView(view, 2131558496, "field 'updateCurrentNumberButton'");
  }

  @Override public void unbind(T target) {
    target._declineResponseTextView = null;
    target._declineResponseEditText = null;
    target._acceptResponseTextView = null;
    target._acceptResponsetEditText = null;
    target.textMessageCheckBox = null;
    target.cloudMessageCheckBox = null;
    target.selectAllowSaveCheckBox = null;
    target.selectFindFriendsCheckBox = null;
    target.currentPhoneNumberTextView = null;
    target.updateCurrentNumberButton = null;
  }
}
