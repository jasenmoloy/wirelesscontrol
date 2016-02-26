package jasenmoloy.wirelesscontrol.mvp;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class MainPresenterImpl implements MainPresenter {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    /// ----------------------
    /// Object Fields
    /// ----------------------

    MainModel mModel;
    MainView mView;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public MainPresenterImpl(MainView view) {
        mView = view;
        mModel = new MainModelImpl();
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
