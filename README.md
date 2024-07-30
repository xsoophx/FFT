# Kymatik: A Kotlin Library for Audio Analysis 🎵🧪

Named after the fascinating study of wave phenomena, Kymatik offers a suite to analyze Audio data. It contains a range
of functionalities, including accurate BPM detection and comprehensive FFT analysis. Beyond its capabilities with audio
files, Kymatik's FFT methods can be applied to samples of various origins, not limited to audio data.
This flexibility allows for a broader scope of analysis and manipulation. Kymatik aims to facilitate a deeper
exploration of .wav files and beyond, offering the tools necessary for detailed examination and manipulation of complex
waveforms.

# Contents

- [Kymatik: A Kotlin Library for Audio Analysis 🎵🧪](#kymatik-a-kotlin-library-for-audio-analysis-)
    * [Current Features](#current-features)
- [Upcoming Features 🚧](#upcoming-features-)
- [Getting Started 🚀](#getting-started-)
    + [1. Installation](#1-installation)
    + [2. Usage](#2-usage)
        - [Kymatik with Koin Dependency Injection](#kymatik-with-koin-dependency-injection)
        - [Kymatik without Koin Dependency Injection](#kymatik-without-koin-dependency-injection)
- [Contributing](#contributing)
- [Feedback and Support](#feedback-and-support)
- [License](#license)
- [BPM Analyzer 🎛️](#bpm-analyzer-)

# Current Features

## 1. FFT Analysis

Kymatik offers comprehensive Fast Fourier Transform (FFT) methods that allow you to break down .wav audio files into
their constituent frequencies. You can also use Kymatik to analyze your own samples with the help of the FFT methods.
The currently supported FFT methods include:

- [X] Cooley-Tukey Algorithm
- [X] [In Place Cooley-Tukey Algorithm](https://en.wikipedia.org/w/index.php?title=Cooley%E2%80%93Tukey_FFT_algorithm#Data_reordering,_bit_reversal,_and_in-place_algorithms)
- [X] Discrete Fourier Transform (DFT)
- [X] Inverse Fast Fourier Transform (IFFT)

## 2. Efficient and Precise BPM Detection

See [BPM Analyzer 🎛️](#bpm-analyzer-)

## 3. Stretching / compressing audio files

Kymatik enables the extension or reduction of audio file durations. However, this currently affects the pitch of the
audio file. Efforts are underway to develop a pitch correction feature that will permit the alteration of audio file
lengths without impacting their pitch. This functionality is particularly valuable for beat synchronization in remixes
or modifying the pace of a track.

# Upcoming Features 🚧

I'm excited about the roadmap ahead and are actively working on expanding Kymatik's capabilities.
Some of the features currently in development include:

### 1. Expanding FFT Analysis:

- Windowed FFT: Adding FFT Hop size as sample number (currently only supports hop size in seconds, e.g. 0.1s)
    - Current solution: hopIntervalInSeconds = hopSizeInSamples / sampleRate
- Zero Padding: Zero padding the input signal to improve the frequency resolution of the FFT
- Bluestein Algorithm
- Goertzel Algorithm

### 2. Starting Position Detection:

A feature that will allow you to detect the starting position of a beat in a music file.
This can be useful for removing silence at the beginning of a track or aligning beats in a remix.

### 3. Various pitch detection / shifting algorithms:

Including YIN, Harmonic Product Spectrum and more.

### 4. .mp3 and .flac support:

Support for additional audio formats, allowing to analyze a wider range of audio files but also converting them to
other formats.

### 5. Audio visualization:

Visualize the audio data in various ways, including waveform, spectrogram and more.

### 6. Documentation:

Comprehensive documentation as a guide through the library's features and functionalities.

**Please note that these features are in various stages of development and will be rolled out as they reach maturity.
I'm committed to ensuring that each new feature meets high standards for reliability and performance.**

# Getting Started 🚀

To get started with Kymatik, please follow these steps:

## 1. Installation

To install Kymatik in your project, add the following dependency to your `build.gradle.kts` file:

```kotlin
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation("cc.suffro:kymatik:0.1.0")
}
```

## 2. Usage

Kymatik is using Koin as its dependency injection framework. In the following examples, you can see how to use Kymatik
with and without Koin.

## Kymatik without Koin Dependency Injection

If you prefer not to use Koin, you can also use Kymatik without it.
Here's an example of how to use Kymatik without Koin:

### Call `KoinManager.INSTANCE` to initialize Kyamitks Koin dependency injection framework:

```kotlin
class Main {

    init {
        KoinManager.INSTANCE
    }
}
```

### Reading a .wav file and analyzing its BPM:

```kotlin
val wav = WAVReader.read("path/to/your/wav/file.wav")
val result = BpmAnalyzer().analyze(wav)
```

### Reading a .wav file and calculating its FFT:

```kotlin
    fun calculateFFT() {
    val wav = WAVReader.read("path/to/your/wav/file.wav")
    val params = WindowProcessingParams(
        start = 0.0,
        end = 10.0,
        interval = 0.01,
        channel = 0,
        numSamples = FftSampleSize.DEFAULT
    )
    val fftResult = FFTProcessor.processWav(wav, params, WindowFunctionType.HAMMING.function)
}
```

### Adjusting the tempo of a .wav file:

```kotlin

```

This method is yielding a sequence of Frequency Domain Windows, each containing the FFT result of the respective time
window.

### Calculating the FFT of your custom samples:

```kotlin
  fun calculateFftOfCustom() {
    val samples = (0 until 1024).map { i -> i.toDouble() }
    val fftResult = FFTProcessor.process(samples, 44100)
}
```

### Using your custom window function and a non-default FFT method:

```kotlin
 fun calculateWithCustomFunction() {
    val samples = (0 until 1024).map { i -> i.toDouble() }
    val customFunction: WindowFunction = { sample, length -> sample.toDouble() / length }

    val fftResult = FFTProcessor.process(
        inputSamples = samples,
        samplingRate = 44100,
        method = Method.R2C_DFT,
        windowFunction = customFunction
    )
}
```

## Kymatik with Koin Dependency Injection

**Important: Make sure to initialize Koin before using it. This can be done through instantiating the class
or by inheriting from it. Alternatively, you can also call `KoinManager.INSTANCE` to initialize Koin.**

All the examples from above can be used with Koin as well. Here's an example of how to use Kymatik with Koin:

### 1. Implement KoinComponent and call `KoinManager.INSTANCE` to initialize Koin:

```kotlin
class Main : KoinComponent {

    init {
        KoinManager.INSTANCE
    }
}
```

Alternatively you can also create a class that inherits from `BPMAnalyzer` or instantiate an instance of it, which is
then taking care of the Koin startup and shutdown in its init function:

```kotlin
class Main : BpmAnalyzer() {
    // Your code here
}
```

### 2. In your class, you can now use the Koin dependency injection to get your desired services.

**Important: Make sure to stop Koin after you're done analyzing your audio files. This can be achieved by calling
`close()`on the BPMAnalyzer instance or using the `use` function (see example above). Alternatively, you can also call
`KoinManager.INSTANCE.close()` to stop Koin.**

```kotlin
class Main : BpmAnalyzer() {

    fun analyze(wav: Wav): TrackInfo {
        val fileReader: FileReader<Wav> by inject()
        val wav = fileReader.read("path/to/your/wav/file.wav")

        return use { analyze(wav) }
    }
}
```

or

```kotlin
class Main : KoinComponent {

    init {
        KoinManager.INSTANCE
    }

    fun analyze(wav: Wav): TrackInfo {
        val bpmAnalyzer: BPMAnalyzer by inject()
        val fileReader: FileReader<Wav> by inject()
        val wav = fileReader.read("path/to/your/wav/file.wav")

        return bpmAnalyzer.use { it.analyze(wav) }
    }
}
```

# Contributing

Contributions are warmly welcomed. Whether it's feature development, bug fixes or documentation improvements.
Please refer to the contributing guidelines for more information.

# Feedback and Support

For feature requests, bug reports or general feedback, please open an issue on this GitHub repository.

# License

Kymatik is licensed under the MIT License, allowing for widespread use and adaptation.
For full license details, please see the LICENSE file.

# BPM Analyzer 🎛️

This project was originally designed to analyze the tempo (BPM) of .wav music files, utilizing various digital signal
processing techniques. The primary method used for tempo detection in this repository is implemented in the
CombFilterAnalyzer, which is giving high precision in determining BPM.

The Analyzer is making use of an algorithm, which was proposed
[on this page](https://www.clear.rice.edu/elec301/Projects01/beat_sync/beatalgo.html) -
see [CombFilterAnalyzer](src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/analyzers/CombFilterAnalyzer.kt).

### Step 1: [Filter bank](src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/filters/Filterbank.kt)

The algorithm begins by dissecting the audio signal into distinct frequency bands, isolating different instrumental
ranges. This step is important, as it mitigates the potential for tempo detection errors caused by overlapping beats
from various instruments. By applying the
[Fast Fourier Transform](src/main/kotlin/cc/suffro/bpmanalyzer/fft/FFTProcessor.kt) (FFT) and segmenting the resultant
spectrum into predefined frequency ranges, each band captures a unique aspect of the music's profile
(0-200Hz to 3200Hz). This is ensuring a comprehensive analysis across the spectrum.

### Step 2: Smoothing

Each frequency band undergoes full-wave rectification followed by a convolution with an optional
[window function](src/main/kotlin/cc/suffro/bpmanalyzer/fft/data/WindowFunction.kt) (a process for smoothing out
the signal and accentuating the amplitudes). This smoothing helps to have a cleaner representation of the rhythmic
pulse.

### Step 3: Differential Rectification

The algorithm now [differentiates](src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/filters/DifferentialRectifier.kt)
the signals to highlight sudden changes in amplitude.
By differentiating and then half-wave rectifying, the algorithm determines significant sound intensity increases, which
typically align with the beats in music.
This step transforms the smoothed envelopes into a form optimized for the final tempo analysis.

### Step 4: Comb Filter

Finally, the algorithm uses a
[comb filter](src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/filters/CombFilter.kt) to scan through the
differentiated signals. This comb filter is convolved with the signal to determine the alignment between the signal's
rhythmic pattern and the filter's tempo. When the tempo of the comb filter resonates with the tempo of the music, the
convolution results in a signal with pronounced peaks, indicating a strong correlation. By examining the energy output
of these convolutions across a spectrum of tempos, the algorithm can accurately determine the music's tempo.
