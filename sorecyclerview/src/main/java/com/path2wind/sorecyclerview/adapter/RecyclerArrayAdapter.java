package com.path2wind.sorecyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ${DESC}
 *
 * @author Terry
 * @time 16/9/9 09:20
 * email path2wind@gmail.com
 */
abstract public class RecyclerArrayAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    protected List<T> mObjects;
    protected EventDelegate mEventDelegate;
    protected ArrayList<ItemView> headers = new ArrayList<>();
    protected ArrayList<ItemView> footers = new ArrayList<>();

    protected OnItemClickListener mItemClickListener;
    protected OnItemLongClickListener mItemLongClickListener;

    RecyclerView.AdapterDataObserver mObserver;

    public interface ItemView {
        View onCreateView(ViewGroup parent);
        void onBindView(View headerView);
    }
    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public class GridSpanSizeLookup extends GridLayoutManager.SpanSizeLookup{
        private int mMaxCount;
        public GridSpanSizeLookup(int maxCount){
            this.mMaxCount = maxCount;
        }
        @Override
        public int getSpanSize(int position) {
            if (headers.size()!=0){
                if (position<headers.size())return mMaxCount;
            }
            if (footers.size()!=0) {
                int i = position - headers.size() - mObjects.size();
                if (i >= 0) {
                    return mMaxCount;
                }
            }
            return 1;
        }
    }

    public GridSpanSizeLookup obtainGridSpanSizeLookUp(int maxCount){
        return new GridSpanSizeLookup(maxCount);
    }

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();


    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    private Context mContext;


    /**
     * Constructor
     *
     * @param context The current context.
     */
    public RecyclerArrayAdapter(Context context) {
        init(context,  new ArrayList<T>());
    }


    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public RecyclerArrayAdapter(Context context, T[] objects) {
        init(context, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public RecyclerArrayAdapter(Context context, List<T> objects) {
        init(context, objects);
    }


    private void init(Context context , List<T> objects) {
        mContext = context;
        mObjects = objects;
    }


    public void stopMore(){
        if (mEventDelegate == null)throw new NullPointerException("You should invoking setLoadMore() first");
        mEventDelegate.stopLoadMore();
    }

    public void pauseMore(){
        if (mEventDelegate == null)throw new NullPointerException("You should invoking setLoadMore() first");
        mEventDelegate.pauseLoadMore();
    }

    public void resumeMore(){
        if (mEventDelegate == null)throw new NullPointerException("You should invoking setLoadMore() first");
        mEventDelegate.resumeLoadMore();
    }


    public void addHeader(ItemView view){
        if (view==null)throw new NullPointerException("ItemView can't be null");
        headers.add(view);
        notifyItemInserted(footers.size()-1);
    }

    public void addFooter(ItemView view){
        if (view==null)throw new NullPointerException("ItemView can't be null");
        footers.add(view);
        notifyItemInserted(headers.size()+getCount()+footers.size()-1);
    }

    public void removeAllHeader(){
        int count = headers.size();
        headers.clear();
        notifyItemRangeRemoved(0,count);
    }

    public void removeAllFooter(){
        int count = footers.size();
        footers.clear();
        notifyItemRangeRemoved(headers.size()+getCount(),count);
    }

    public ItemView getHeader(int index){
        return headers.get(index);
    }

    public ItemView getFooter(int index){
        return footers.get(index);
    }

    public int getHeaderCount(){return headers.size();}

    public int getFooterCount(){return footers.size();}

    public void removeHeader(ItemView view){
        int position = headers.indexOf(view);
        headers.remove(view);
        notifyItemRemoved(position);
    }

    public void removeFooter(ItemView view){
        int position = headers.size()+getCount()+footers.indexOf(view);
        footers.remove(view);
        notifyItemRemoved(position);
    }


    EventDelegate getEventDelegate(){
        if (mEventDelegate == null)mEventDelegate  = new DefaultEventDelegate(this);
        return mEventDelegate;
    }

    public View setMore(final int res, final OnLoadMoreListener listener){
        FrameLayout container = new FrameLayout(getContext());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(getContext()).inflate(res, container);
        getEventDelegate().setMore(container, listener);
        return container;
    }

    public View setMore(final View view,OnLoadMoreListener listener){
        getEventDelegate().setMore(view, listener);
        return view;
    }

    public View setNoMore(final int res) {
        FrameLayout container = new FrameLayout(getContext());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(getContext()).inflate(res, container);
        getEventDelegate().setNoMore(container);
        return container;
    }

    public View setNoMore(final View view) {
        getEventDelegate().setNoMore(view);
        return view;
    }

    public View setError(final int res) {
        FrameLayout container = new FrameLayout(getContext());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(getContext()).inflate(res, container);
        getEventDelegate().setErrorMore(container);
        return container;
    }

    public View setError(final View view) {
        getEventDelegate().setErrorMore(view);
        return view;
    }


    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        if (observer instanceof EasyRecyclerView.EasyDataObserver){
            mObserver = observer;
        }else {
            super.registerAdapterDataObserver(observer);
        }
    }


    public void add(T object) {
        if (mEventDelegate!=null)mEventDelegate.addData(object == null ? 0 : 1);
        if (object!=null){
            synchronized (mLock) {
                mObjects.add(object);
            }
        }
        if (mObserver!=null)mObserver.onItemRangeInserted(getCount()+1,1);
        if (mNotifyOnChange) notifyItemInserted(headers.size()+getCount()+1);
    }

    public void addAll(Collection<? extends T> collection) {
        if (mEventDelegate!=null)mEventDelegate.addData(collection == null ? 0 : collection.size());
        if (collection!=null&&collection.size()!=0){
            synchronized (mLock) {
                mObjects.addAll(collection);
            }
        }
        int dataCount = collection==null?0:collection.size();
        if (mObserver!=null)mObserver.onItemRangeInserted(getCount()-dataCount+1,dataCount);
        if (mNotifyOnChange) notifyItemRangeInserted(headers.size()+getCount()-dataCount+1,dataCount);

    }


    public void addAll(T[] items) {
        if (mEventDelegate!=null)mEventDelegate.addData(items==null?0:items.length);
        if (items!=null&&items.length!=0) {
            synchronized (mLock) {
                Collections.addAll(mObjects, items);
            }
        }
        int dataCount = items==null?0:items.length;
        if (mObserver!=null)mObserver.onItemRangeInserted(getCount()-dataCount+1,dataCount);
        if (mNotifyOnChange) notifyItemRangeInserted(headers.size()+getCount()-dataCount+1,dataCount);
    }


    public void insert(T object, int index) {
        synchronized (mLock) {
            mObjects.add(index, object);
        }
        if (mObserver!=null)mObserver.onItemRangeInserted(index,1);
        if (mNotifyOnChange) notifyItemInserted(headers.size()+index+1);
    }


    public void insertAll(T[] object, int index) {
        synchronized (mLock) {
            mObjects.addAll(index, Arrays.asList(object));
        }
        int dataCount = object==null?0:object.length;
        if (mObserver!=null)mObserver.onItemRangeInserted(index+1,dataCount);
        if (mNotifyOnChange) notifyItemRangeInserted(headers.size()+index+1,dataCount);
    }


    public void insertAll(Collection<? extends T> object, int index) {
        synchronized (mLock) {
            mObjects.addAll(index, object);
        }
        int dataCount = object==null?0:object.size();
        if (mObserver!=null)mObserver.onItemRangeInserted(index+1,dataCount);
        if (mNotifyOnChange) notifyItemRangeInserted(headers.size()+index+1,dataCount);
    }


    public void removeAll(Collection<? extends T> objects, int position){
        synchronized (mLock) {
            if (mObjects.removeAll(objects)){
                if (mObserver!=null) mObserver.onItemRangeRemoved(position,objects.size());
//                if (mNotifyOnChange) notifyItemRemoved(headers.size()+position);
            }
        }
    }


    public void remove(T object) {
        int position = mObjects.indexOf(object);
        synchronized (mLock) {
            if (mObjects.remove(object)){
                if (mObserver!=null)mObserver.onItemRangeRemoved(position,1);
                if (mNotifyOnChange) notifyItemRemoved(headers.size()+position);
            }
        }
    }


    public void remove(int position) {
        synchronized (mLock) {
            mObjects.remove(position);
        }
        if (mObserver!=null)mObserver.onItemRangeRemoved(position,1);
        if (mNotifyOnChange) notifyItemRemoved(headers.size()+position);
    }



    public void clear() {
        int count = mObjects.size();
        if (mEventDelegate!=null)mEventDelegate.clear();
        synchronized (mLock) {
            mObjects.clear();
        }
        if (mObserver!=null)mObserver.onItemRangeRemoved(0,count);
        if (mNotifyOnChange) notifyItemRangeRemoved(headers.size(),count);
    }


    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            Collections.sort(mObjects, comparator);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }


    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }


    public Context getContext() {
        return mContext;
    }

    public void setContext(Context ctx) {
        mContext = ctx;
    }


    @Deprecated
    @Override
    public final int getItemCount() {
        return mObjects.size()+headers.size()+footers.size();
    }


    public int getCount(){
        return mObjects.size();
    }

    private View createSpViewByType(ViewGroup parent, int viewType){
        for (ItemView headerView:headers){
            if (headerView.hashCode() == viewType){
                View view = headerView.onCreateView(parent);
                StaggeredGridLayoutManager.LayoutParams layoutParams;
                if (view.getLayoutParams()!=null)
                    layoutParams = new StaggeredGridLayoutManager.LayoutParams(view.getLayoutParams());
                else
                    layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setFullSpan(true);
                view.setLayoutParams(layoutParams);
                return view;
            }
        }
        for (ItemView footerview:footers){
            if (footerview.hashCode() == viewType){
                View view = footerview.onCreateView(parent);
                StaggeredGridLayoutManager.LayoutParams layoutParams;
                if (view.getLayoutParams()!=null)
                    layoutParams = new StaggeredGridLayoutManager.LayoutParams(view.getLayoutParams());
                else
                    layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setFullSpan(true);
                view.setLayoutParams(layoutParams);
                return view;
            }
        }
        return null;
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = createSpViewByType(parent, viewType);
        if (view!=null){
            return new StateViewHolder(view);
        }

        final BaseViewHolder viewHolder = OnCreateViewHolder(parent, viewType);

        //itemView 的点击事件
        if (mItemClickListener!=null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mItemClickListener.onItemClick(viewHolder.getAdapterPosition()-headers.size());
                }
            });
        }

        if (mItemLongClickListener!=null){
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mItemLongClickListener.onItemClick(viewHolder.getAdapterPosition()-headers.size());
                }
            });
        }
        return viewHolder;
    }

    abstract public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType);


    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.itemView.setId(position);
        if (headers.size()!=0 && position<headers.size()){
            headers.get(position).onBindView(holder.itemView);
            return ;
        }

        int i = position - headers.size() - mObjects.size();
        if (footers.size()!=0 && i>=0){
            footers.get(i).onBindView(holder.itemView);
            return ;
        }
        OnBindViewHolder(holder,position-headers.size());
    }


    public void OnBindViewHolder(BaseViewHolder holder, final int position){
        holder.setData(getItem(position));
    }


    @Deprecated
    @Override
    public final int getItemViewType(int position) {
        if (headers.size()!=0){
            if (position<headers.size())return headers.get(position).hashCode();
        }
        if (footers.size()!=0){
            /*
            eg:
            0:header1
            1:header2   2
            2:object1
            3:object2
            4:object3
            5:object4
            6:footer1   6(position) - 2 - 4 = 0
            7:footer2
             */
            int i = position - headers.size() - mObjects.size();
            if (i >= 0){
                return footers.get(i).hashCode();
            }
        }
        return getViewType(position-headers.size());
    }

    public int getViewType(int position){
        return 0;
    }


    public List<T> getAllData(){
        return new ArrayList<>(mObjects);
    }

    /**
     * {@inheritDoc}
     */
    public T getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }


    public long getItemId(int position) {
        return position;
    }

    private class StateViewHolder extends BaseViewHolder{

        public StateViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mItemLongClickListener = listener;
    }


}
