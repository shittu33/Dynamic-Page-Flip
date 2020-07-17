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
