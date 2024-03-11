# Unleashing the power of GeminiPro in your android app – with text + images as an input and text as an output of Gemini by using GeminiPro as well as GeminiProVision models as SDK!

https://github.com/terminator712/Gemini-Multi-turn-Chat/assets/83265437/4fa0c0e4-a021-45b2-b896-5e1bd4649ebe


Sample android app with GeminiPro for multi-turn conversation which supports text + images as an input! 

How to setup: 
1. Import released AAR in your project[Or download module]. How to - https://developer.android.com/studio/projects/android-library#psd-add-aar-jar-dependency
2. Go to **https://aistudio.google.com/** and get your API key
3. In your `local.properties` insert the key like this: `apiKey=YOUR_API_KEY`
4. Initiate interator like this: `val interactor = Interactor.getInstance(WeakReference(this))`
5. Init the chat and get UIState: `val uiState = interactor.initChat(BuildConfig.apiKey, safetySetting /*optional*/)`
    You can customize your safetySetting like this :
   
       val safetySetting = listOf(
            SafetySetting(
                harmCategory = HarmCategory.HARASSMENT,
                threshold = BlockThreshold.MEDIUM_AND_ABOVE
            ),
            SafetySetting(
                harmCategory = HarmCategory.HATE_SPEECH,
                threshold = BlockThreshold.MEDIUM_AND_ABOVE
            ),
            SafetySetting(
                harmCategory = HarmCategory.DANGEROUS_CONTENT,
                threshold = BlockThreshold.LOW_AND_ABOVE
            ),
            SafetySetting(
                harmCategory = HarmCategory.SEXUALLY_EXPLICIT,
                threshold = BlockThreshold.LOW_AND_ABOVE
            )
        )
6. To send message: `interactor.sendMessage(message, images /*optional can be empty*/)`. UIState will be get updated when the response of model arrives. 
7. Build project and run!
    

