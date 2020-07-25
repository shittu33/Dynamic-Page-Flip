# Dynamic-Page-Flip
A dynamic flip View that accept adapter to populate it child Views like  a View Pager

## DEMO
![alt text](https://github.com/shittu33/Dynamic-Page-Flip/blob/master/single.gif?raw=true)![alt text](https://github.com/shittu33/Dynamic-Page-Flip/blob/master/multi.gif?raw=true)![alt text](https://github.com/shittu33/Dynamic-Page-Flip/blob/master/speed.gif?raw=true)

## Usage

#### XML Declearation
declear dynamic FlipView from xml
```xml
<com.example.adaptablecurlpage.flipping.views.DynamicFlipView
      android:id="@+id/dynamic_flip_view"
      android:layout_width="match_parent"                   
      android:layout_height="match_parent" 
      app:flip_speed="very_slow"
      app:shadow_type="normal"
      app:page_type="magazine"
      app:page_back_alpha="0.4"/>
```
#### Initialization
Initialize the dynamic FlipView from xml in your Activity
```JAVA
dynamicFlipView= findViewById(R.id.dynamic_flip_view)
```
Initialize the dynamic FlipView directly in your Activity
```JAVA
  dynamicFlipView= new DynamicFlipView(context);
```
## Data Binding

### Homogenous Pages

To use a single layout in pages

#### Kotlin 
```kotlin
for (i in 1..15) {
   pagesData.add("This is Page $i")
}
dynamic_flip_view.loadSingleLayoutPages(R.layout.scroll_text_item, pagesData) { position, data
   ->
   tV.setText(data)
}
```
#### Java
```Java
for (int i = 1; i < 16; i++) {
   pages_data.add("Some text");
}
dynamicFlipView.loadSingleLayoutPages(R.layout.scroll_text_item, pages_data
    , new DynamicFlipView.HandleSingleViewCallback<String>() {
      @Override
      public void HandleView(View v, int position, String data) {
         final EditText tV = v.findViewById(R.id.tV);
         tV.setText(data);
      }
   });
```
### Heterogenous Pages

To use a different layout in pages
#### Kotlin 

```kotlin
val dataList = LinkedList<Pair<Int, MutableMap<Int, *>>>()
dataList.add(R.layout.item1 with mutableMapOf(R.id.tV to "Let's \n Begin!", R.id.img to R.drawable.google_fun))
dataList.add(R.layout.item2 with mutableMapOf(R.id.tV to "Get Ready!!", R.id.img to R.drawable.dance))
dataList.add(R.layout.scroll_text_item with mutableMapOf(R.id.tV to getRubbishText()))
dataList.add(R.layout.item_simple with mutableMapOf(R.id.img to R.drawable.dance))
dynamic_flip_view.loadMultiLayoutPages(dataList) { position, data, layout
     ->
      when (layout) {
             R.layout.item1,
             R.layout.item2 -> click()//only item1 & 2 has button to click
             R.layout.item_simple,
             R.layout.scroll_image_item -> {
   //                    TODO("Do anything specific " +
   //                            "from (item_simple to scroll_image_item")
             }
         }
         //Handle anything common with all view
         for (viewId in data.keys)
             loadData(viewId)
}
```
### Java

```
LinkedList<Pair<Integer, PageData>> list = new LinkedList<>();
list.add(new Pair<>(R.layout.item1, new PageData(R.id.img, R.drawable.google_fun)));
list.add(new Pair<>(R.layout.item_simple, new PageData(R.id.img, R.drawable.dance)));
list.add(new Pair<>(R.layout.item2, new PageData(R.id.img, R.drawable.dance)));
dynamicFlipView.loadMultiLayoutPages(list, new DynamicFlipView.HandleMultiViewCallback<PageData>() {
@Override
public void HandleView(View v, final int position, final PageData data, @LayoutRes int layout) {
    final ImageView img = v.findViewById(data.id);
    final Button btn = v.findViewById(R.id.btn);
    switch (layout) {
        case R.layout.item1:
        case R.layout.item2:
            final EditText tV = v.findViewById(R.id.tV);
            tV.setText("Image");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String alertMsg = data.id + "of position" + position;
                    tV.setText(alertMsg);
                }
            });
        case R.layout.item_simple:
            ViewUtils.loadImageWithGlide(data.result, img);
    }
}
});
```
### Customization

To load data in to DynamiFlipView like normal ListView or GridView, create an Adapter and attach it to the view

```Java
dynamicFlipView.setAdapter(adapter);
```

Change View parameters
```Java
dynamicFlipView.setFlipSpeed(FlipSpeed.NORMAL)
    .setMaxBackAlpha(0.5f)
    .setPageBackColor(Color.BLACK)
    .setPageBackColorToDominant()
    .setPageType(PageType.MAGAZINE_SHEET)
    .setPageShadowType(PageShadowType.NO_SHADOW)
```

setUp Flip Listeners or callbacks

```java
dynamicFlipView.setFlipLister(new DynamicFlipView.OnPageFlippedListener() {
@Override
    public void onPageFlipped(View page, int page_no, long id) {
        Log.e(TAG, "onPageFlipped: page no is " + page_no);
    }

    @Override
    public void onPageFlippedBackward(View page, int page_no, long id) {
        Log.e(TAG, "onPageFlippedBackward: page no is " + page_no);
    }

    @Override
    public void onPageFlippedForward(View page, int page_no, long id) {
        Log.e(TAG, "onPageFlippedForward: page no is " + page_no);
    }

    @Override
    public void onFingerDown(View v, int pos) {
        Log.e(TAG, "onFingerDown: on page " + pos);
    }

    @Override
    public void onFingerUp(View v, int pos) {
        Log.e(TAG, "onFingerUp: on page " + pos);
    }

    @Override
    public void onFingerDownToFlip(View page, int page_no) {
        Log.e(TAG, "onFingerDownToFlip: on page " + page_no);
    }

    @Override
    public void onFingerUpToFlip(View page, int page_no) {
        Log.e(TAG, "onFingerUpToFlip: on page " + page_no);
    }

    @Override
    public void onFastFlipStart(View page, int page_no, boolean is_forward) {
        Log.e(TAG, "onFastFlipStart: page no is " + page_no);
    }

    @Override
    public void onFastFlipEnd(View page, int page_no, boolean is_forward) {
        Log.e(TAG, "onFastFlipEnd: page no is " + page_no);
    }                    
})
```
#### Gestures

##### Click to Flip
Instead of dragging your finger accross the page to flip, sometimes its nice to just click on the right edge of the page to flip forward and left edge of the page to flip backward(like that in whatsApp/Facebook statuses) 
##### Fast Flip
Instead of just a click on the edges, sometimes you just want to hold your hand on those edges to flip fast forward and backward the pages.

DynamicFlipView support this two type of gestures!!!


## Credit
Eschao Page Flip


### License
```
Copyright 2020 Abdulmujeeb Olawale
Licensed under the Apache License, Version 2.0

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
