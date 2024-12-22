import type {HostComponent, ViewProps} from 'react-native';
import type {BubblingEventHandler} from 'react-native/Libraries/Types/CodegenTypes';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

type CameraLoadEvent = {
  result: 'succcess' | 'error';
};

export interface WebViewProps extends ViewProps {
  onCameraReady?: BubblingEventHandler<CameraLoadEvent> | null;
}

export default codegenNativeComponent<WebViewProps>(
  'NativeCamera',
) as HostComponent<WebViewProps>;
