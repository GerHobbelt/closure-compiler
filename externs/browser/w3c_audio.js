/*
 * Copyright 2012 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Definitions for the API related to audio.
 * Definitions for the Web Audio API.
 * This file is based on the W3C Working Draft 08 December 2015.
 * @see http://www.w3.org/TR/webaudio/
 *
 * @externs
 */

/**
 * @constructor
 */
function BaseAudioContext() {};

/** 
 * @type {!AudioDestinationNode}
 * @readonly
 */
BaseAudioContext.prototype.destination;

/** 
 * @type {number}
 * @readonly
 */
BaseAudioContext.prototype.sampleRate;

/**
 * @type {number}
 * @readonly
 */
BaseAudioContext.prototype.currentTime;

/**
 * @type {!AudioListener}
 * @readonly
 */
BaseAudioContext.prototype.listener;

/**
 * @readonly
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#BaseAudioContext for valid values
 */
BaseAudioContext.prototype.state;

/**
 * @return {!Promise<void>}
 */
BaseAudioContext.prototype.suspend = function() {};

/**
 * @return {!Promise<void>}
 */
BaseAudioContext.prototype.resume = function() {};

/**
 * @return {!Promise<void>}
 */
BaseAudioContext.prototype.close = function() {};

/** @type {EventTarget} */
BaseAudioContext.prototype.onstatechange;

/**
 * @param {number} numberOfChannels
 * @param {number} length
 * @param {number} sampleRate
 * @return {!AudioBuffer}
 */
BaseAudioContext.prototype.createBuffer =
    function(numberOfChannels, length, sampleRate) {};

/**
 * @param {ArrayBuffer} audioData
 * @param {function(AudioBuffer)=} successCallback
 * @param {function(?)=} errorCallback
 * @return {!Promise<AudioBuffer>}
 */
BaseAudioContext.prototype.decodeAudioData =
    function(audioData, successCallback, errorCallback) {};

/**
 * @return {!AudioBufferSourceNode}
 */
BaseAudioContext.prototype.createBufferSource = function() {};

/**
 * @param  {string} scriptURL
 * @return {!Promise<AudioWorker>}
 */
BaseAudioContext.prototype.createAudioWorker = function(scriptURL) {};

/**
 * @param {number} bufferSize
 * @param {number=} numberOfInputChannels_opt
 * @param {number=} numberOfOutputChannels_opt
 * @return {!ScriptProcessorNode}
 */
BaseAudioContext.prototype.createScriptProcessor = function(bufferSize,
    numberOfInputChannels_opt, numberOfOutputChannels_opt) {};

/**
 * @return {!RealtimeAnalyserNode}
 */
BaseAudioContext.prototype.createAnalyser = function() {};

/**
 * @return {!GainNode}
 */
BaseAudioContext.prototype.createGain = function() {};

/**
 * @param {number=} maxDelayTime
 * @return {!DelayNode}
 */
BaseAudioContext.prototype.createDelay = function(maxDelayTime) {};

/**
 * @return {!BiquadFilterNode}
 */
BaseAudioContext.prototype.createBiquadFilter = function() {};

/**
 * @param  {number} feedforward
 * @param  {number} feedback
 * @return {!IIRFilterNode}
 */
BaseAudioContext.prototype.createIIRFilter = function(feedforward, feedback) {};

/**
 * @return {!WaveShaperNode}
 */
BaseAudioContext.prototype.createWaveShaper = function() {};

/**
 * @return {!PannerNode}
 */
BaseAudioContext.prototype.createPanner = function() {};

/**
 * @return {!SpatialPannerNode}
 */
BaseAudioContext.prototype.createSpatialPanner = function() {};
/**
 * @return {!StereoPannerNode}
 */
BaseAudioContext.prototype.createStereoPanner = function() {};

/**
 * @return {!ConvolverNode}
 */
BaseAudioContext.prototype.createConvolver = function() {};

/**
 * @param {number=} numberOfOutputs
 * @return {!AudioChannelSplitter}
 */
BaseAudioContext.prototype.createChannelSplitter = function(numberOfOutputs) {};

/**
 * @return {!DynamicsCompressorNode}
 */
BaseAudioContext.prototype.createDynamicsCompressor = function() {};

/**
 * @return {!OscillatorNode}
 */
BaseAudioContext.prototype.createOscillator = function() {};

/**
 * @param {Float32Array} real
 * @param {Float32Array} imag
 * @return {!PeriodicWave}
 */
BaseAudioContext.prototype.createPeriodicWave = function(real, imag) {};

/**
 * @constructor
 * @extends {BaseAudioContext}
 */
function AudioContext() {};

/**
 * @param {HTMLMediaElement} mediaElement
 * @return {!MediaElementAudioSourceNode}
 */
AudioContext.prototype.createMediaElementSource = function(mediaElement) {};

/**
 * @return {!MediaStreamAudioDestinationNode}
 */
AudioContext.prototype.createMediaStreamDestination = function() {};

/**
 * @param {MediaStream} mediaStream
 * @return {!MediaStreamAudioSourceNode}
 */
AudioContext.prototype.createMediaStreamSource = function(mediaStream) {};

/**
 * @deprecated Use AudioContext#createGain
 */
AudioContext.prototype.createGainNode = function() {};

/**
 * @param {number} numberOfChannels
 * @param {number} length
 * @param {number} sampleRate
 * @constructor
 * @extends {BaseAudioContext}
 */
function OfflineAudioContext(numberOfChannels, length, sampleRate) {}

OfflineAudioContext.prototype.startRendering = function() {};

/** @type {function(OfflineAudioCompletionEvent)} */
OfflineAudioContext.prototype.oncomplete;

/**
 * @return {!Promise<void>}
 */
OfflineAudioContext.prototype.suspend = function() {};

/**
 * @return {!Promise<void>}
 */
OfflineAudioContext.prototype.resume = function() {};

/**
 * @constructor
 * @extends {Event}
 */
function OfflineAudioCompletionEvent() {}

/** @type {AudioBuffer} */
OfflineAudioCompletionEvent.prototype.renderedBuffer;

/**
 * @constructor
 */
function AudioNode() {}

/**
 * @param {AudioNode|AudioParam} destination
 * @param {number=} output
 * @param {number=} input
 * @return {AudioNode|void}
 */
AudioNode.prototype.connect = function(destination, output, input) {};

/**
 * @param {AudioNode|AudioParam|number=} destination
 * @param {number=} output
 * @param {number=} input
 */
AudioNode.prototype.disconnect = function(destination, output, input) {};

/** 
 * @type {!AudioContext}
 * @readonly
 */
AudioNode.prototype.context;

/** 
 * @type {number}
 * @readonly
 */
AudioNode.prototype.numberOfInputs;

/**
 * @type {number}
 * @readonly
 */
AudioNode.prototype.numberOfOutputs;

/** @type {number} */
AudioNode.prototype.channelCount;

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-audionode-interface for valid values
 */
AudioNode.prototype.channelCountMode;

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-audionode-interface for valid values
 */
AudioNode.prototype.channelInterpretation;

/**
 * @constructor
 * @extends {AudioNode}
 */
function AudioDestinationNode() {}

/**
 * To be deprecated. Use maxChannelCount instead.
 * @type {number}
 */
AudioDestinationNode.prototype.numberOfChannels;

/** @type {number} */
AudioDestinationNode.prototype.maxChannelCount;

/**
 * @constructor
 */
function AudioParam() {}

/** @type {number} */
AudioParam.prototype.value;

/** 
 * @type {number}
 * @readonly
 */
AudioParam.prototype.defaultValue;

/**
 * @param {number} value
 * @param {number} startTime
 */
AudioParam.prototype.setValueAtTime = function(value, startTime) {};

/**
 * @param {number} value
 * @param {number} endTime
 */
AudioParam.prototype.linearRampToValueAtTime = function(value, endTime) {};

/**
 * @param {number} value
 * @param {number} endTime
 */
AudioParam.prototype.exponentialRampToValueAtTime = function(value, endTime) {};

/**
 * @param {number} target
 * @param {number} startTime
 * @param {number} timeConstant
 */
AudioParam.prototype.setTargetAtTime = function(target, startTime,
    timeConstant) {};

/**
 * @deprecated Use setTargetAtTime instead.
 * @param {number} target
 * @param {number} startTime
 * @param {number} timeConstant
 */
AudioParam.prototype.setTargetValueAtTime = function(target, startTime,
    timeConstant) {};

/**
 * @param {Float32Array} values
 * @param {number} startTime
 * @param {number} duration
 */
AudioParam.prototype.setValueCurveAtTime = function(values, startTime,
    duration) {};

/**
 * @param {number} startTime
 */
AudioParam.prototype.cancelScheduledValues = function(startTime) {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function GainNode() {}

/** 
 * @type {!AudioParam}
 * @readonly
 */
GainNode.prototype.gain;

/**
 * @constructor
 * @extends {AudioNode}
 */
function DelayNode() {}

/** 
 * @type {!AudioParam}
 * @readonly
 */
DelayNode.prototype.delayTime;

/**
 * @constructor
 */
function AudioBuffer() {}

/** 
 * @readonly
 * @type {number}
 */
AudioBuffer.prototype.sampleRate;

/**
 * @readonly
 * @type {number}
 */
AudioBuffer.prototype.length;

/**
 * @readonly
 * @type {number}
 */
AudioBuffer.prototype.duration;

/**
 * @readonly
 * @type {number}
 */
AudioBuffer.prototype.numberOfChannels;

/**
 * @param {number} channel
 * @return {Float32Array}
 */
AudioBuffer.prototype.getChannelData = function(channel) {};

/**
 * @param  {Float32Array} destination
 * @param  {number} channelNumber
 * @param  {number=} startInChannel
 */
AudioBuffer.prototype.copyFromChannel = function(destination,
    channelNumber, startInChannel) {};

/**
 * @param  {Float32Array} source
 * @param  {number} channelNumber
 * @param  {number=} startInChannel
 */
AudioBuffer.prototype.copyToChannel = function(source, channelNumber,
    startInChannel) {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function AudioBufferSourceNode() {}

/** @type {AudioBuffer} */
AudioBufferSourceNode.prototype.buffer;

/**
 * @type {!AudioParam}
 * @readonly
 */
AudioBufferSourceNode.prototype.playbackRate;

/**
 * @type {!AudioParam}
 * @readonly
 */
AudioBufferSourceNode.prototype.detune;

/** @type {boolean} */
AudioBufferSourceNode.prototype.loop;

/** @type {number} */
AudioBufferSourceNode.prototype.loopStart;

/** @type {number} */
AudioBufferSourceNode.prototype.loopEnd;

/**
 * @param {number=} when
 * @param {number=} opt_offset
 * @param {number=} opt_duration
 */
AudioBufferSourceNode.prototype.start = function(when, opt_offset,
    opt_duration) {};

/** 
 * @param {number=} when
 * @param {number=} opt_offset
 * @param {number=} opt_duration
 * @deprecated Use AudioBufferSourceNode#start
 */
AudioBufferSourceNode.prototype.noteGrainOn = function(when, opt_offset,
    opt_duration) {};

/**
 * @param {number} when
 */
AudioBufferSourceNode.prototype.stop = function(when) {};

/** 
 * @param {number} when
 * @deprecated Use AudioBufferSourceNode#stop
 */
AudioBufferSourceNode.prototype.noteOff = function(when) {};

/** @type {EventTarget} */
AudioBufferSourceNode.prototype.onended;

/**
 * @constructor
 * @extends {AudioNode}
 */
function MediaElementAudioSourceNode() {};

/**
 * @constructor
 */
function AudioWorker() {};

/**
 */
AudioWorker.prototype.terminate = function() {};

/**
 * @param  {*} message
 * @param  {Array<Transferable>=} transfer
 */
AudioWorker.prototype.postMessage = function(message, transfer) {};

/**
 * @type {Array<AudioWorkerParamDescriptor>}
 * @readonly
 */
AudioWorker.prototype.parameters;

/** @type {EventTarget} */
AudioWorker.prototype.onmessage;

/** @type {EventTarget} */
AudioWorker.prototype.onloaded;

/**
 * @param  {number} numberOfInputs
 * @param  {number} numberOfOutputs
 * @return {AudioWorkerNode}
 */
AudioWorker.prototype.createNode = function(numberOfInputs, numberOfOutputs) {};

/**
 * @param  {string} name
 * @param  {number} defaultValue
 * @return {AudioParam}
 */
AudioWorker.prototype.addParameter = function(name, defaultValue) {};

/**
 * @param  {string} name
 */
AudioWorker.prototype.removeParameter = function(name) {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function AudioWorkerNode() {};

/**
 * @param  {*} message
 * @param  {Array<Transferable>=} transfer
 */
AudioWorkerNode.prototype.postMessage = function(message, transfer) {};

/** @type {EventTarget} */
AudioWorkerNode.prototype.onmessage;

/**
 * @constructor
 */
function AudioWorkerParamDescriptor() {};

/**
 * @type {string}
 * @readonly
 */
AudioWorkerParamDescriptor.prototype.name;

/**
 * @type {number}
 * @readonly
 */
AudioWorkerParamDescriptor.prototype.defaultValue;

/**
 * @constructor
 */
function AudioWorkerGlobalScope() {};

/** 
 * @type {number}
 * @readonly
 */
AudioWorkerGlobalScope.prototype.sampleRate;

/**
 * @param  {string} name
 * @param  {number} defaultValue
 * @return {AudioParam}
 */
AudioWorkerGlobalScope.prototype.addParameter = function(name, defaultValue) {};

/**
 * @param  {string} name
 */
AudioWorkerGlobalScope.prototype.removeParameter = function(name) {};

/** @type {EventTarget} */
AudioWorkerGlobalScope.prototype.onaudioprocess;

/** @type {EventTarget} */
AudioWorkerGlobalScope.prototype.onnodecreate;

/**
 * @type {Array<AudioWorkerParamDescriptor>}
 * @readonly
 */
AudioWorkerGlobalScope.prototype.parameters;

/**
 * @constructor
 */
function AudioWorkerNodeProcessor() {};

/**
 * @param  {*} message
 * @param  {Array<Transferable>=} transfer
 */
AudioWorkerNodeProcessor.prototype.postMessage = function(message, transfer) {};

/** @type {EventTarget} */
AudioWorkerNodeProcessor.prototype.onmessage;

/**
 * @constructor
 * @extends {AudioNode}
 * @deprecated Use AudioWorkerNode
 */
function ScriptProcessorNode() {}

/**
 * @type {EventListener|(function(!AudioProcessingEvent):(boolean|undefined))}
 * @deprecated Use AudioWorkerNode
 */
ScriptProcessorNode.prototype.onaudioprocess;

/**
 * @const
 * @type {number}
 * @deprecated Use AudioWorkerNode
 */
ScriptProcessorNode.prototype.bufferSize;

/**
 * @constructor
 * @extends {Event}
 */
function AudioWorkerNodeCreationEvent() {};

/**
 * @type {AudioWorkerNodeProcessor}
 * @readonly
 */
AudioWorkerNodeCreationEvent.prototype.node;

/**
 * @type {Array}
 * @readonly
 */
AudioWorkerNodeCreationEvent.prototype.inputs;

/**
 * @type {Array}
 * @readonly
 */
AudioWorkerNodeCreationEvent.prototype.outputs;

/**
 * @constructor
 * @extends {Event}
 */
function AudioProcessEvent() {};

/**
 * @type {number}
 * @readonly
 */
AudioProcessEvent.prototype.playbackTime;

/**
 * @type {AudioWorkerNodeProcessor}
 * @readonly
 */
AudioProcessEvent.prototype.node;

/**
 * @type {Float32Array}
 * @readonly
 */
AudioProcessEvent.prototype.inputs;

/**
 * @type {Float32Array}
 * @readonly
 */
AudioProcessEvent.prototype.outputs;

/**
 * @type {Object}
 * @readonly
 */
AudioProcessEvent.prototype.parameters;

/**
 * @constructor
 * @extends {Event}
 * @deprecated Use AudioProcessEvent
 */
function AudioProcessingEvent() {}

/** 
 * @type {ScriptProcessorNode}
 * @deprecated Use AudioProcessEvent
 */
AudioProcessingEvent.prototype.node;

/** 
 * @type {number}
 * @deprecated Use AudioProcessEvent
 */
AudioProcessingEvent.prototype.playbackTime;

/** 
 * @type {AudioBuffer}
 * @deprecated Use AudioProcessEvent
 */
AudioProcessingEvent.prototype.inputBuffer;

/**
 * @type {AudioBuffer}
 * @deprecated Use AudioProcessEvent
 */
AudioProcessingEvent.prototype.outputBuffer;

/**
 * @constructor
 * @extends {AudioNode}
 */
function PannerNode() {}

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-pannernode-interface for valid values
 */
PannerNode.prototype.panningModel;

/** @type {!AudioParam} */
PannerNode.prototype.positionX;

/** @type {!AudioParam} */
PannerNode.prototype.positionY;

/** @type {!AudioParam} */
PannerNode.prototype.positionZ;

/** @type {!AudioParam} */
PannerNode.prototype.orientationX;

/** @type {!AudioParam} */
PannerNode.prototype.orientationY;

/** @type {!AudioParam} */
PannerNode.prototype.orientationZ;

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-pannernode-interface for valid values
 */
PannerNode.prototype.distanceModel;

/** @type {number} */
PannerNode.prototype.refDistance;

/** @type {number} */
PannerNode.prototype.maxDistance;

/** @type {number} */
PannerNode.prototype.rolloffFactor;

/** @type {number} */
PannerNode.prototype.coneInnerAngle;

/** @type {number} */
PannerNode.prototype.coneOuterAngle;

/** @type {number} */
PannerNode.prototype.coneOuterGain;

/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
PannerNode.prototype.setPosition = function(x, y, z) {};

/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
PannerNode.prototype.setOrientation = function(x, y, z) {};

/**
 * @constructor
 * @deprecated Use SpatialListener
 */
function AudioListener() {}

/**
 * @type {number}
 * @deprecated Use SpatialListener
 */
AudioListener.prototype.gain;

/** 
 * @type {number}
 * @deprecated Use SpatialListener
 */
AudioListener.prototype.dopplerFactor;

/**
 * @type {number}
 * @deprecated Use SpatialListener
 */
AudioListener.prototype.speedOfSound;

/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 * @deprecated Use SpatialListener
 */
AudioListener.prototype.setPosition = function(x, y, z) {};

/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 * @param {number} xUp
 * @param {number} yUp
 * @param {number} zUp
 * @deprecated Use SpatialListener
 */
AudioListener.prototype.setOrientation = function(x, y, z, xUp, yUp, zUp) {};

/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 * @deprecated Use SpatialListener
 */
AudioListener.prototype.setVelocity = function(x, y, z) {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function SpatialPannerNode() {};

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-pannernode-interface for valid values
 */
SpatialPannerNode.prototype.panningModel; 

/** 
 * @type {!AudioParam} 
 * @readonly
 */
SpatialPannerNode.prototype.positionX;

/** 
 * @type {!AudioParam} 
 * @readonly
 */
SpatialPannerNode.prototype.positionY;

/** 
 * @type {!AudioParam} 
 * @readonly
 */
SpatialPannerNode.prototype.positionZ;

/** 
 * @type {!AudioParam} 
 * @readonly
 */
SpatialPannerNode.prototype.orientationX;

/** 
 * @type {!AudioParam} 
 * @readonly
 */
SpatialPannerNode.prototype.orientationY;

/** 
 * @type {!AudioParam} 
 * @readonly
 */
SpatialPannerNode.prototype.orientationZ;

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-pannernode-interface for valid values
 */
SpatialPannerNode.prototype.distanceModel;
 
/** @type {number} */
SpatialPannerNode.prototype.refDistance;

/** @type {number} */
SpatialPannerNode.prototype.maxDistance;

/** @type {number} */
SpatialPannerNode.prototype.rolloffFactor;

/** @type {number} */
SpatialPannerNode.prototype.coneInnerAngle;

/** @type {number} */
SpatialPannerNode.prototype.coneOuterAngle;

/** @type {number} */
SpatialPannerNode.prototype.coneOuterGain;

/**
 * @constructor
 */
function SpatialListener() {};

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.positionX;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.positionY;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.positionZ;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.forwardX;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.forwardY;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.forwardZ;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.upX;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.upY;

/**
 * @type {!AudioParam}
 * @readonly
 */
SpatialListener.prototype.upZ;

/**
 * @constructor
 * @extends {AudioNode}
 * @see http://webaudio.github.io/web-audio-api/#the-stereopannernode-interface
 */
function StereoPannerNode() {}

/** 
 * @type {!AudioParam}
 * @readonly
 */
StereoPannerNode.prototype.pan;

/**
 * @constructor
 * @extends {AudioNode}
 */
function ConvolverNode() {}

/** @type {?AudioBuffer} */
ConvolverNode.prototype.buffer;

/** @type {boolean} */
ConvolverNode.prototype.normalize;

/**
 * @constructor
 * @extends {AudioNode}
 */
var AnalyserNode = function() {};

/**
 * @param {Float32Array} array
 */
AnalyserNode.prototype.getFloatFrequencyData = function(array) {};

/**
 * @param {Uint8Array} array
 */
AnalyserNode.prototype.getByteFrequencyData = function(array) {};

/**
 * @param  {Float32Array} array
 */
AnalyserNode.prototype.getFloatTimeDomainData = function(array) {};

/**
 * @param {Uint8Array} array
 */
AnalyserNode.prototype.getByteTimeDomainData = function(array) {};

/** @type {number} */
AnalyserNode.prototype.fftSize;

/** @type {number} */
AnalyserNode.prototype.frequencyBinCount;

/** @type {number} */
AnalyserNode.prototype.minDecibels;

/** @type {number} */
AnalyserNode.prototype.maxDecibels;

/** @type {number} */
AnalyserNode.prototype.smoothingTimeConstant;

/**
 * @constructor
 * @extends {AnalyserNode}
 * @deprecated Use AnalyserNode
 *
 * This constructor has been added for backwards compatibility.
 */
var RealtimeAnalyserNode = function() {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function ChannelSplitterNode() {};

/**
 * @constructor
 * @extends {ChannelSplitterNode}
 * @deprecated Use ChannelSplitterNode
 *
 * This constructor has been added for backwards compatibility.
 */
function AudioChannelSplitter() {}

/**
 * @constructor
 * @extends {AudioNode}
 */
function ChannelMergerNode() {};

/**
 * @constructor
 * @extends {ChannelMergerNode}
 * @deprecated Use ChannelMergerNode
 *
 * This constructor has been added for backwards compatibility.
 */
function AudioChannelMerger() {}

/**
 * @constructor
 * @extends {AudioNode}
 */
function DynamicsCompressorNode() {}

/** 
 * @type {!AudioParam}
 * @readonly
 */
DynamicsCompressorNode.prototype.threshold;

/** 
 * @type {!AudioParam}
 * @readonly
 */
DynamicsCompressorNode.prototype.knee;

/** 
 * @type {!AudioParam}
 * @readonly
 */
DynamicsCompressorNode.prototype.ratio;

/** 
 * @type {!number}
 * @readonly
 */
DynamicsCompressorNode.prototype.reduction;

/** 
 * @type {!AudioParam}
 * @readonly
 */
DynamicsCompressorNode.prototype.attack;

/** 
 * @type {!AudioParam}
 * @readonly
 */
DynamicsCompressorNode.prototype.release;

/**
 * @constructor
 * @extends {AudioNode}
 */
function BiquadFilterNode() {}

/**
 * A read-able and write-able string that specifies the type of the filter.
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-biquadfilternode-interface for valid values
 */
BiquadFilterNode.prototype.type;

/** @type {!AudioParam} */
BiquadFilterNode.prototype.frequency;

/** @type {!AudioParam} */
BiquadFilterNode.prototype.detune;

/** @type {!AudioParam} */
BiquadFilterNode.prototype.Q;

/** @type {!AudioParam} */
BiquadFilterNode.prototype.gain;

/**
 * @param {Float32Array} frequencyHz
 * @param {Float32Array} magResponse
 * @param {Float32Array} phaseResponse
 */
BiquadFilterNode.prototype.getFrequencyResponse = function(frequencyHz,
    magResponse, phaseResponse) {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function IIRFilterNode() {};

/**
 * @param {Float32Array} frequencyHz
 * @param {Float32Array} magResponse
 * @param {Float32Array} phaseResponse
 */
IIRFilterNode.prototype.getFrequencyResponse = function(frequencyHz,
    magResponse, phaseResponse) {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function WaveShaperNode() {}

/** @type {Float32Array} */
WaveShaperNode.prototype.curve;

/** @type {string} */
WaveShaperNode.prototype.oversample;

/**
 * @constructor
 * @extends {AudioNode}
 */
function OscillatorNode() {}

/** 
 * @type {string}
 * See https://www.w3.org/TR/webaudio/#the-oscillatornode-interface for valid values
 */
OscillatorNode.prototype.type;

/** @type {!AudioParam} */
OscillatorNode.prototype.frequency;

/** @type {!AudioParam} */
OscillatorNode.prototype.detune;

/**
 * @param {number=} when
 */
OscillatorNode.prototype.start = function(when) {};

/**
 * @param {number=} when
 */
OscillatorNode.prototype.stop = function(when) {};

/**
 * @param {PeriodicWave} periodicWave
 */
OscillatorNode.prototype.setPeriodicWave = function(periodicWave) {};

/** @type {EventTarget} */
OscillatorNode.prototype.onended;

/**
 * @constructor
 */
function PeriodicWave() {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function MediaStreamAudioSourceNode() {};

/**
 * @constructor
 * @extends {AudioNode}
 */
function MediaStreamAudioDestinationNode() {};

/**
 * @type {!MediaStream}
 * @readonly
 */
MediaStreamAudioDestinationNode.prototype.stream;

/**
 * Definitions for the Web Audio API with webkit prefix.
 */

/**
 * @constructor
 * @extends {AudioContext}
 */
function webkitAudioContext() {};

/**
 * @param {number} numberOfChannels
 * @param {number} length
 * @param {number} sampleRate
 * @constructor
 * @extends {OfflineAudioContext}
 */
function webkitOfflineAudioContext(numberOfChannels, length, sampleRate) {};

/**
 * @constructor
 * @extends {PannerNode}
 */
function webkitPannerNode() {};

/**
 * Definitions for the Audio API as implemented in Firefox.
 *   Please note that this document describes a non-standard experimental API.
 *   This API is considered deprecated.
 * @see https://developer.mozilla.org/en/DOM/HTMLAudioElement
 */

/**
 * @param {string=} src
 * @constructor
 * @extends {HTMLAudioElement}
 */
function Audio(src) {};

/**
 * @param {number} channels
 * @param {number} rate
 */
Audio.prototype.mozSetup = function(channels, rate) {};

/**
 * @param {Array|Float32Array} buffer
 */
Audio.prototype.mozWriteAudio = function(buffer) {};

/**
 * @return {number}
 */
Audio.prototype.mozCurrentSampleOffset = function() {};