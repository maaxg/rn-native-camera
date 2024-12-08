import type {HostComponent, ViewProps} from 'react-native';
import type {BubblingEventHandler} from 'react-native/Libraries/Types/CodegenTypes';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

type WebViewScriptLoadEvent = {
  result: 'succcess' | 'error';
};

export interface WebViewProps extends ViewProps {
  sourceURL: string;
  onScriptLoaded?: BubblingEventHandler<WebViewScriptLoadEvent> | null;
}

export default codegenNativeComponent<WebViewProps>(
  'CustomWebView',
) as HostComponent<WebViewProps>;
