# Dynamic-Page-Flip
A dynamic flip View that accept adapter to populate it child Views like  a View Pager

## DEMO
![alt text](https://github.com/shittu33/Dynamic-Page-Flip/blob/master/single.gif?raw=true)![alt text](https://github.com/shittu33/Dynamic-Page-Flip/blob/master/multi.gif?raw=true)![alt text](https://github.com/shittu33/Dynamic-Page-Flip/blob/master/speed.gif?raw=true)

## Usage

To use a single layout for all pages
#### Kotlin 
```kotlin
for (i in 1..15) {
   pagesData.add("This is Page $i")
}
dynamic_flip_view.setFlipSpeed(speed)
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
