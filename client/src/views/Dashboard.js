import React from 'react';
import ChartistGraph from 'react-chartist';
// react-bootstrap components
import {
  Badge,
  Navbar,
  Nav,
  Container,
  Row,
  Col,
  Form,
  OverlayTrigger,
  Tooltip,
} from 'react-bootstrap';
import {
  Tile,
  Button,
  Grid,
  Column,
  FlexGrid,
  DataTable,
  Link,
} from 'carbon-components-react';

const {
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableBody,
  TableCell,
  TableHeader,
  TableSelectAll,
  TableSelectRow,
  TableToolbar,
  TableBatchActions,
  TableToolbarContent,
  TableToolbarSearch,
  TableBatchAction,
  TableToolbarMenu,
  TableToolbarAction,
} = DataTable;
// import CustomDataTable from '../components/CustomTable';
// import {
//   rowsMany as demoRowsMany,
//   columns as demoColumns,
//   sortInfo as demoSortInfo,
// } from './table-data';
// import { Link } from 'react-router-dom';

const initialRows = [];

const headers = [
  {
    key: 'hospital',
    header: 'Hospital',
  },
  {
    key: 'protocol',
    header: 'Protocol',
  },
  {
    key: 'port',
    header: 'Port',
  },
  {
    key: 'rule',
    header: 'Rule',
  },
  {
    key: 'attached_groups',
    header: 'Attached Groups',
  },
  {
    key: 'status',
    header: 'Status',
  },
];

function Dashboard() {
  const handleSelectAll = selectAll => () => {
    selectAll();
  };
  return (
    <>
      <Container fluid>
        <div>
          {/* <DataTable rows={initialRows} headers={headers}>
            {({
              rows,
              headers,
              getHeaderProps,
              getRowProps,
              getSelectionProps,
              getToolbarProps,
              getBatchActionProps,
              onInputChange,
              selectedRows,
              getTableProps,
              getTableContainerProps,
            }) => {
              const batchActionProps = getBatchActionProps();

              return (
                <TableContainer
                  title='DataTable'
                  description='With batch actions'
                  {...getTableContainerProps()}
                >
                  <TableToolbar {...getToolbarProps()}>
                    <TableBatchActions {...batchActionProps}>
                      <TableBatchAction
                        tabIndex={
                          batchActionProps.shouldShowBatchActions ? 0 : -1
                        }
                        // renderIcon={TrashCan}
                        onClick={() => selectedRows}
                      >
                        Delete
                      </TableBatchAction>
                      <TableBatchAction
                        tabIndex={
                          batchActionProps.shouldShowBatchActions ? 0 : -1
                        }
                        // renderIcon={Save}
                        onClick={() => selectedRows}
                      >
                        Save
                      </TableBatchAction>
                      <TableBatchAction
                        tabIndex={
                          batchActionProps.shouldShowBatchActions ? 0 : -1
                        }
                        // renderIcon={Download}
                        onClick={() => selectedRows}
                      >
                        Download
                      </TableBatchAction>
                    </TableBatchActions>
                    <TableToolbarContent
                      aria-hidden={batchActionProps.shouldShowBatchActions}
                    >
                      <TableToolbarSearch
                        tabIndex={
                          batchActionProps.shouldShowBatchActions ? -1 : 0
                        }
                        onChange={onInputChange}
                      />
                      <TableToolbarMenu
                        tabIndex={
                          batchActionProps.shouldShowBatchActions ? -1 : 0
                        }
                      >
                        <TableToolbarAction onClick={() => alert('Alert 1')}>
                          Action 1
                        </TableToolbarAction>
                        <TableToolbarAction onClick={() => alert('Alert 2')}>
                          Action 2
                        </TableToolbarAction>
                        <TableToolbarAction onClick={() => alert('Alert 3')}>
                          Action 3
                        </TableToolbarAction>
                      </TableToolbarMenu>
                      <Button
                        tabIndex={
                          batchActionProps.shouldShowBatchActions ? -1 : 0
                        }
                        onClick={() => 'Add new row'}
                        size='small'
                        kind='primary'
                      >
                        Add new
                      </Button>
                    </TableToolbarContent>
                  </TableToolbar>
                  <Table {...getTableProps()}>
                    <TableHead>
                      <TableRow>
                        <TableSelectAll {...getSelectionProps()} />
                        {headers.map((header, i) => (
                          <TableHeader key={i} {...getHeaderProps({ header })}>
                            {header.header}
                          </TableHeader>
                        ))}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rows.map((row, i) => (
                        <TableRow key={i} {...getRowProps({ row })}>
                          <TableSelectRow {...getSelectionProps({ row })} />
                          {row.cells.map(cell => (
                            <TableCell key={cell.id}>{cell.value}</TableCell>
                          ))}
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              );
            }}
          </DataTable> */}
        </div>
      </Container>
    </>
  );
}

export default Dashboard;
